/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.application;

import static com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor.DEFAULT_DOMAIN_NAME;
import static com.zernova.test.allure.AllureConstants.DeployableCreationFeature.APP_CREATION;

import static java.util.Optional.empty;

import static org.hamcrest.Matchers.instanceOf;

import static org.junit.Assert.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.memory.management.MemoryManagementService;
import com.zernova.runtime.api.service.ServiceRepository;
import com.zernova.runtime.deployment.model.api.application.Application;
import com.zernova.runtime.deployment.model.api.application.ApplicationDescriptor;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactConfigurationProcessor;
import com.zernova.runtime.deployment.model.api.builder.ApplicationClassLoaderBuilder;
import com.zernova.runtime.deployment.model.api.builder.ApplicationClassLoaderBuilderFactory;
import com.zernova.runtime.deployment.model.api.domain.Domain;
import com.zernova.runtime.deployment.model.api.domain.DomainDescriptor;
import com.zernova.runtime.deployment.model.api.plugin.resolver.PluginDependenciesResolver;
import com.zernova.runtime.module.artifact.activation.api.descriptor.DeployableArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository;
import com.zernova.runtime.module.artifact.activation.internal.classloader.ZernovaApplicationClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderRepository;
import com.zernova.runtime.module.deployment.impl.internal.domain.DefaultDomainManager;
import com.zernova.runtime.module.deployment.impl.internal.domain.DomainManager;
import com.zernova.runtime.module.deployment.impl.internal.policy.PolicyTemplateClassLoaderBuilderFactory;
import com.zernova.runtime.module.license.api.LicenseValidator;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;

@Feature(APP_CREATION)
@Issue("W-11911617")
public class ApplicationsCreatedWithDeprecatedDescriptorClassTestCase extends AbstractZernovaTestCase {

  private final ApplicationClassLoaderBuilderFactory applicationClassLoaderBuilderFactory =
      mock(ApplicationClassLoaderBuilderFactory.class);
  private final DomainManager domainManager = new DefaultDomainManager();
  private final DefaultApplicationFactory applicationFactory =
      new DefaultApplicationFactory(applicationClassLoaderBuilderFactory,
                                    DeployableArtifactDescriptorFactory.defaultArtifactDescriptorFactory(),
                                    domainManager,
                                    mock(ServiceRepository.class),
                                    mock(ExtensionModelLoaderRepository.class),
                                    mock(ClassLoaderRepository.class),
                                    mock(PolicyTemplateClassLoaderBuilderFactory.class),
                                    mock(PluginDependenciesResolver.class),
                                    mock(LicenseValidator.class),
                                    mock(MemoryManagementService.class),
                                    mock(ArtifactConfigurationProcessor.class));

  public ApplicationsCreatedWithDeprecatedDescriptorClassTestCase() {
    ApplicationClassLoaderBuilder applicationClassLoaderBuilderMock = mock(ApplicationClassLoaderBuilder.class);
    when(applicationClassLoaderBuilderMock.setDomainParentClassLoader(any())).thenReturn(applicationClassLoaderBuilderMock);
    when(applicationClassLoaderBuilderMock.setArtifactDescriptor(any()))
        .thenReturn(applicationClassLoaderBuilderMock);
    when(applicationClassLoaderBuilderMock.build()).thenReturn(mock(ZernovaApplicationClassLoader.class));
    when(applicationClassLoaderBuilderFactory.createArtifactClassLoaderBuilder())
        .thenReturn(applicationClassLoaderBuilderMock);

    createDefaultDomain();
  }

  private void createDefaultDomain() {
    final Domain defaultDomain = mock(Domain.class);

    final ArtifactClassLoader domainArtifactClassLoader = mock(ArtifactClassLoader.class);
    when(domainArtifactClassLoader.getClassLoader()).thenReturn(mock(ClassLoader.class));
    when(defaultDomain.getArtifactClassLoader()).thenReturn(domainArtifactClassLoader);
    when(defaultDomain.getDescriptor()).thenReturn(new DomainDescriptor(DEFAULT_DOMAIN_NAME));

    domainManager.addDomain(defaultDomain);
  }

  @Test
  public void heavyweightApplicationCreatedWithDeprecatedDescriptorClass() throws Exception {
    Application application =
        applicationFactory.createArtifact(getApplicationFolder("apps/no-dependencies-heavyweight"), empty());

    assertThat(application.getDescriptor(), instanceOf(ApplicationDescriptor.class));
  }

  @Test
  public void lightweightApplicationCreatedWithDeprecatedDescriptorClass() throws Exception {
    Application application = applicationFactory.createArtifact(getApplicationFolder("apps/no-dependencies"), empty());

    assertThat(application.getDescriptor(), instanceOf(ApplicationDescriptor.class));
  }

  protected File getApplicationFolder(String path) throws URISyntaxException {
    return new File(getClass().getClassLoader().getResource(path).toURI());
  }

}
