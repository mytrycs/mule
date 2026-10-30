/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.domain.test;

import static com.zernova.runtime.module.artifact.activation.internal.deployable.AbstractDeployableProjectModelBuilder.defaultDeployableProjectModelBuilder;
import static com.zernova.runtime.module.artifact.activation.internal.deployable.AbstractDeployableProjectModelBuilder.isHeavyPackage;
import static com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor.DEFAULT_DOMAIN_NAME;
import static com.zernova.test.allure.AllureConstants.DeployableCreationFeature.DOMAIN_CREATION;
import static com.zernova.test.allure.AllureConstants.DeploymentTypeFeature.DeploymentTypeStory.HEAVYWEIGHT;

import static java.util.Optional.empty;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.memory.management.MemoryManagementService;
import com.zernova.runtime.api.service.ServiceRepository;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactConfigurationProcessor;
import com.zernova.runtime.deployment.model.api.builder.DomainClassLoaderBuilder;
import com.zernova.runtime.deployment.model.api.builder.DomainClassLoaderBuilderFactory;
import com.zernova.runtime.deployment.model.api.domain.Domain;
import com.zernova.runtime.deployment.model.api.domain.DomainDescriptor;
import com.zernova.runtime.deployment.model.internal.domain.test.AbstractDomainTestCase;
import com.zernova.runtime.module.artifact.activation.api.deployable.DeployableProjectModel;
import com.zernova.runtime.module.artifact.activation.api.descriptor.DeployableArtifactDescriptorCreator;
import com.zernova.runtime.module.artifact.activation.api.descriptor.DeployableArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository;
import com.zernova.runtime.module.artifact.activation.internal.classloader.ZernovaApplicationClassLoader;
import com.zernova.runtime.module.artifact.activation.internal.deployable.AbstractDeployableProjectModelBuilder;
import com.zernova.runtime.module.artifact.activation.internal.deployable.ZernovaDeployableProjectModelBuilder;
import com.zernova.runtime.module.deployment.impl.internal.domain.DefaultDomainFactory;
import com.zernova.runtime.module.deployment.impl.internal.domain.DefaultDomainManager;
import com.zernova.runtime.module.deployment.impl.internal.domain.DomainDescriptorFactory;
import com.zernova.runtime.module.deployment.impl.internal.domain.EmptyDomainDescriptor;
import com.zernova.runtime.module.license.api.LicenseValidator;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(DOMAIN_CREATION)
@Story(HEAVYWEIGHT)
public class DefaultDomainFactoryTestCase extends AbstractDomainTestCase {

  private final ServiceRepository serviceRepository = mock(ServiceRepository.class);
  private final DeployableArtifactDescriptorFactory deployableArtifactDescriptorFactory =
      mock(DeployableArtifactDescriptorFactory.class);
  private final DomainClassLoaderBuilderFactory domainClassLoaderBuilderFactory = mock(DomainClassLoaderBuilderFactory.class);
  private final ExtensionModelLoaderRepository extensionModelLoaderRepository = mock(ExtensionModelLoaderRepository.class);
  private final LicenseValidator licenseValidator = mock(LicenseValidator.class);
  private final DefaultDomainFactory domainFactory = new DefaultDomainFactory(mock(DomainDescriptorFactory.class),
                                                                              deployableArtifactDescriptorFactory,
                                                                              new DefaultDomainManager(),
                                                                              null,
                                                                              serviceRepository,
                                                                              domainClassLoaderBuilderFactory,
                                                                              extensionModelLoaderRepository,
                                                                              licenseValidator,
                                                                              mock(MemoryManagementService.class),
                                                                              mock(ArtifactConfigurationProcessor.class));
  private MockedStatic<AbstractDeployableProjectModelBuilder> utilities;

  public DefaultDomainFactoryTestCase() throws IOException {}

  @Before
  public void setUp() throws Exception {
    utilities = mockStatic(AbstractDeployableProjectModelBuilder.class);
    utilities.when(() -> isHeavyPackage(any())).thenReturn(true);
    utilities.when(() -> defaultDeployableProjectModelBuilder(any(), any(), anyBoolean())).thenCallRealMethod();
    DeployableProjectModel deployableProjectModelMock = mock(DeployableProjectModel.class);
    doNothing().when(deployableProjectModelMock).validate();
    ZernovaDeployableProjectModelBuilder muleDeployableProjectModelBuilderMock =
        mock(ZernovaDeployableProjectModelBuilder.class);
    when(muleDeployableProjectModelBuilderMock.build()).thenReturn(deployableProjectModelMock);
    mockConstruction(ZernovaDeployableProjectModelBuilder.class);
  }

  @Test
  public void createDefaultDomain() throws IOException {
    final ZernovaApplicationClassLoader domainArtifactClassLoader = mock(ZernovaApplicationClassLoader.class);
    when(domainArtifactClassLoader.getArtifactId()).thenReturn(DEFAULT_DOMAIN_NAME);

    DomainClassLoaderBuilder domainClassLoaderBuilderMock = mock(DomainClassLoaderBuilder.class);
    when(domainClassLoaderBuilderMock.setArtifactDescriptor(any())).thenReturn(domainClassLoaderBuilderMock);
    when(domainClassLoaderBuilderMock.build()).thenReturn(domainArtifactClassLoader);
    when(domainClassLoaderBuilderFactory.createArtifactClassLoaderBuilder()).thenReturn(domainClassLoaderBuilderMock);

    Domain domain = domainFactory.createArtifact(new File(DEFAULT_DOMAIN_NAME), empty());
    assertThat(domain.getArtifactName(), is(DEFAULT_DOMAIN_NAME));
    assertThat(domain.getDescriptor(), instanceOf(EmptyDomainDescriptor.class));
    assertThat(domain.getArtifactClassLoader(), is(domainArtifactClassLoader));
  }

  @Test
  public void createCustomDomain() throws IOException {
    String domainName = "custom-domain";

    final DomainDescriptor descriptor = new DomainDescriptor(domainName);
    when(deployableArtifactDescriptorFactory.createDomainDescriptor(any(), any(), any(DeployableArtifactDescriptorCreator.class)))
        .thenReturn(descriptor);

    final ZernovaApplicationClassLoader domainArtifactClassLoader = mock(ZernovaApplicationClassLoader.class);
    when(domainArtifactClassLoader.getArtifactId()).thenReturn(domainName);

    DomainClassLoaderBuilder domainClassLoaderBuilderMock = mock(DomainClassLoaderBuilder.class);
    when(domainClassLoaderBuilderMock.setArtifactDescriptor(any()))
        .thenReturn(domainClassLoaderBuilderMock);
    when(domainClassLoaderBuilderMock.build()).thenReturn(domainArtifactClassLoader);
    when(domainClassLoaderBuilderFactory.createArtifactClassLoaderBuilder()).thenReturn(domainClassLoaderBuilderMock);

    Domain domain = domainFactory.createArtifact(new File(domainName), empty());

    assertThat(domain.getArtifactName(), is(domainName));
    assertThat(domain.getDescriptor(), is(descriptor));
    assertThat(domain.getArtifactClassLoader(), is(domainArtifactClassLoader));
  }

  @After
  public void after() {
    utilities.close();
  }

}
