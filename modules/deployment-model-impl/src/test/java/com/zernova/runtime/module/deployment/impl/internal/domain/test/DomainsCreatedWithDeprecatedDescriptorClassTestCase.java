/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.domain.test;

import static com.zernova.test.allure.AllureConstants.DeployableCreationFeature.DOMAIN_CREATION;

import static java.util.Optional.empty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import static org.mockito.Mockito.mock;

import com.zernova.runtime.api.memory.management.MemoryManagementService;
import com.zernova.runtime.api.service.ServiceRepository;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactConfigurationProcessor;
import com.zernova.runtime.deployment.model.api.builder.DomainClassLoaderBuilderFactory;
import com.zernova.runtime.deployment.model.api.domain.Domain;
import com.zernova.runtime.deployment.model.api.domain.DomainDescriptor;
import com.zernova.runtime.module.artifact.activation.api.classloader.ArtifactClassLoaderResolver;
import com.zernova.runtime.module.artifact.activation.api.descriptor.DeployableArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidatorBuilder;
import com.zernova.runtime.module.artifact.internal.util.ServiceRegistryDescriptorLoaderRepository;
import com.zernova.runtime.module.deployment.impl.internal.domain.DefaultDomainFactory;
import com.zernova.runtime.module.deployment.impl.internal.domain.DefaultDomainManager;
import com.zernova.runtime.module.deployment.impl.internal.domain.DomainDescriptorFactory;
import com.zernova.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorLoader;
import com.zernova.runtime.module.license.api.LicenseValidator;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;

@Feature(DOMAIN_CREATION)
@Issue("W-11911617")
public class DomainsCreatedWithDeprecatedDescriptorClassTestCase extends AbstractZernovaTestCase {

  private final DefaultDomainFactory domainFactory =
      new DefaultDomainFactory(new DomainDescriptorFactory(mock(ArtifactPluginDescriptorLoader.class),
                                                           new ServiceRegistryDescriptorLoaderRepository(),
                                                           ArtifactDescriptorValidatorBuilder.builder()),
                               DeployableArtifactDescriptorFactory.defaultArtifactDescriptorFactory(),
                               new DefaultDomainManager(),
                               null,
                               mock(ServiceRepository.class),
                               new DomainClassLoaderBuilderFactory(ArtifactClassLoaderResolver.defaultClassLoaderResolver()),
                               mock(ExtensionModelLoaderRepository.class),
                               mock(LicenseValidator.class),
                               mock(MemoryManagementService.class),
                               mock(ArtifactConfigurationProcessor.class));

  @Test
  public void heavyweightDomainCreatedWithDeprecatedDescriptorClass() throws Exception {
    Domain domain = domainFactory.createArtifact(getDomainFolder("domains/no-dependencies-heavyweight"), empty());

    assertThat(domain.getDescriptor(), instanceOf(DomainDescriptor.class));
  }

  @Test
  public void lightweightDomainCreatedWithDeprecatedDescriptorClass() throws Exception {
    Domain domain = domainFactory.createArtifact(getDomainFolder("domains/no-dependencies"), empty());

    assertThat(domain.getDescriptor(), instanceOf(DomainDescriptor.class));
  }

  protected File getDomainFolder(String path) throws URISyntaxException {
    return new File(getClass().getClassLoader().getResource(path).toURI());
  }

}
