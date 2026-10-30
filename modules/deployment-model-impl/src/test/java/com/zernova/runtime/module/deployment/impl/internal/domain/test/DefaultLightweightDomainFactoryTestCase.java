/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.domain.test;

import static com.zernova.maven.client.api.MavenClientProvider.discoverProvider;
import static com.zernova.test.allure.AllureConstants.DeployableCreationFeature.DOMAIN_CREATION;
import static com.zernova.test.allure.AllureConstants.DeploymentTypeFeature.DeploymentTypeStory.LIGHTWEIGHT;

import static java.util.Optional.empty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import static org.mockito.Mockito.mock;

import com.zernova.runtime.api.memory.management.MemoryManagementService;
import com.zernova.runtime.api.service.ServiceRepository;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactConfigurationProcessor;
import com.zernova.runtime.deployment.model.api.builder.DomainClassLoaderBuilderFactory;
import com.zernova.runtime.deployment.model.api.domain.Domain;
import com.zernova.runtime.deployment.model.api.plugin.ArtifactPlugin;
import com.zernova.runtime.globalconfig.api.GlobalConfigLoader;
import com.zernova.runtime.module.artifact.activation.api.classloader.ArtifactClassLoaderResolver;
import com.zernova.runtime.module.artifact.activation.api.descriptor.DeployableArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidatorBuilder;
import com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor;
import com.zernova.runtime.module.artifact.internal.util.ServiceRegistryDescriptorLoaderRepository;
import com.zernova.runtime.module.deployment.impl.internal.application.ApplicationDescriptorFactoryTestCase;
import com.zernova.runtime.module.deployment.impl.internal.domain.DefaultDomainFactory;
import com.zernova.runtime.module.deployment.impl.internal.domain.DefaultDomainManager;
import com.zernova.runtime.module.deployment.impl.internal.domain.DomainDescriptorFactory;
import com.zernova.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorLoader;
import com.zernova.runtime.module.license.api.LicenseValidator;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.junit4.rule.SystemProperty;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@Feature(DOMAIN_CREATION)
@Story(LIGHTWEIGHT)
@Issue("W-11086334")
public class DefaultLightweightDomainFactoryTestCase extends AbstractZernovaTestCase {

  @Rule
  public SystemProperty repositoryLocation = new SystemProperty("muleRuntimeConfig.maven.repositoryLocation",
                                                                discoverProvider(ApplicationDescriptorFactoryTestCase.class
                                                                    .getClassLoader()).getLocalRepositorySuppliers()
                                                                        .environmentMavenRepositorySupplier().get()
                                                                        .getAbsolutePath());

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

  @Before
  public void before() {
    GlobalConfigLoader.reset();
  }

  @Test
  public void lightweightDomain() throws Exception {
    String domainName = "no-dependencies";
    Domain domain = domainFactory.createArtifact(getDomainFolder("domains/" + domainName), empty());

    assertThat(domain.getDescriptor(), instanceOf(DomainDescriptor.class));
    assertThat(domain.getDescriptor().getName(), is(domainName));
    assertThat(domain.getArtifactPlugins().size(), is(0));
  }

  @Test
  public void lightweightDomainWithDependencies() throws Exception {
    String domainName = "multiple-dependencies";
    Domain domain = domainFactory.createArtifact(getDomainFolder("domains/" + domainName), empty());

    assertThat(domain.getDescriptor(), instanceOf(DomainDescriptor.class));
    assertThat(domain.getDescriptor().getName(), is(domainName));
    List<ArtifactPlugin> plugins = domain.getArtifactPlugins();
    assertThat(plugins.size(), is(2));
    assertThat(plugins, contains(
                                 hasProperty("artifactId", is("domain/" + domainName + "/plugin/empty-plugin")),
                                 hasProperty("artifactId", is("domain/" + domainName + "/plugin/dependant-plugin"))));
  }

  protected File getDomainFolder(String path) throws URISyntaxException {
    return new File(getClass().getClassLoader().getResource(path).toURI());
  }
}
