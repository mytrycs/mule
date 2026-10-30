/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.classloader;

import static com.zernova.test.allure.AllureConstants.ClassloadingIsolationFeature.CLASSLOADING_ISOLATION;
import static com.zernova.test.allure.AllureConstants.ClassloadingIsolationFeature.ClassloadingIsolationStory.CLASSLOADER_GENERATION;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.module.artifact.activation.api.classloader.ArtifactClassLoaderResolver;
import com.zernova.runtime.module.artifact.activation.api.plugin.PluginClassLoaderResolver;
import com.zernova.runtime.module.artifact.activation.api.plugin.PluginDescriptorResolver;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoaderManager;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaDeployableArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.RegionClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@Issue("W-11069995")
@Feature(CLASSLOADING_ISOLATION)
@Story(CLASSLOADER_GENERATION)
public class TrackingArtifactClassLoaderResolverDecoratorTestCase extends AbstractZernovaTestCase {

  private ArtifactClassLoaderManager artifactClassLoaderManager;
  private TrackingArtifactClassLoaderResolverDecorator decorator;

  @Before
  public void setUp() throws Exception {
    artifactClassLoaderManager = mock(ArtifactClassLoaderManager.class);
    ArtifactClassLoaderResolver artifactClassLoaderResolver = mock(ArtifactClassLoaderResolver.class);
    decorator = new TrackingArtifactClassLoaderResolverDecorator(artifactClassLoaderManager, artifactClassLoaderResolver);
    Function<String, ZernovaDeployableArtifactClassLoader> classLoaderFactory =
        (artifactName) -> new ZernovaDeployableArtifactClassLoader(artifactName, new ArtifactDescriptor(artifactName), new URL[0],
                                                                mock(RegionClassLoader.class),
                                                                mock(ClassLoaderLookupPolicy.class));
    Function<String, ZernovaDeployableArtifactClassLoader> classLoaderWithPluginsFactory = (artifactName) -> {
      ZernovaDeployableArtifactClassLoader classLoader = classLoaderFactory.apply(artifactName);
      List<ArtifactClassLoader> pluginClassLoaders = new ArrayList<>();
      pluginClassLoaders.add(classLoaderFactory.apply(artifactName + " Plugin Class loader 1"));
      pluginClassLoaders.add(classLoaderFactory.apply(artifactName + " Plugin Class loader 2"));

      when(classLoader.getArtifactPluginClassLoaders()).thenReturn(pluginClassLoaders);
      return classLoader;
    };

    ZernovaDeployableArtifactClassLoader domainClassLoader1 = classLoaderWithPluginsFactory.apply("Domain Class loader 1");
    ZernovaDeployableArtifactClassLoader domainClassLoader2 = classLoaderWithPluginsFactory.apply("Domain Class loader 2");
    ZernovaDeployableArtifactClassLoader applicationClassLoader1 = classLoaderWithPluginsFactory.apply("Application Class loader 1");
    ZernovaDeployableArtifactClassLoader applicationClassLoader2 = classLoaderWithPluginsFactory.apply("Application Class loader 2");
    ZernovaDeployableArtifactClassLoader applicationClassLoader3 = classLoaderWithPluginsFactory.apply("Application Class loader 3");
    ZernovaDeployableArtifactClassLoader applicationClassLoader4 = classLoaderWithPluginsFactory.apply("Application Class loader 4");
    ZernovaDeployableArtifactClassLoader pluginClassLoader1 = classLoaderWithPluginsFactory.apply("Plugin Class loader 1");
    ZernovaDeployableArtifactClassLoader pluginClassLoader2 = classLoaderWithPluginsFactory.apply("Plugin Class loader 2");

    when(artifactClassLoaderResolver.createDomainClassLoader(any())).thenReturn(domainClassLoader1);
    when(artifactClassLoaderResolver.createDomainClassLoader(any(), any())).thenReturn(domainClassLoader2);
    when(artifactClassLoaderResolver.createApplicationClassLoader(any())).thenReturn(applicationClassLoader1);
    when(artifactClassLoaderResolver.createApplicationClassLoader(any(), any(PluginClassLoaderResolver.class)))
        .thenReturn(applicationClassLoader2);
    when(artifactClassLoaderResolver.createApplicationClassLoader(any(), any(Supplier.class)))
        .thenReturn(applicationClassLoader3);
    when(artifactClassLoaderResolver.createApplicationClassLoader(any(), any(), any(PluginClassLoaderResolver.class)))
        .thenReturn(applicationClassLoader4);
    when(artifactClassLoaderResolver.createMulePluginClassLoader(any(), any(), any())).thenReturn(pluginClassLoader1);
    when(artifactClassLoaderResolver.createMulePluginClassLoader(any(), any(), any(), any())).thenReturn(pluginClassLoader2);
  }

  @Test
  public void registersClassLoaders() {
    verifyClassLoaderRegistered(decorator.createDomainClassLoader(mock(DomainDescriptor.class)));
    verifyClassLoaderRegistered(decorator.createDomainClassLoader(mock(DomainDescriptor.class),
                                                                  mock(PluginClassLoaderResolver.class)));
    verifyClassLoaderRegistered(decorator.createApplicationClassLoader(mock(ApplicationDescriptor.class)));
    verifyClassLoaderRegistered(decorator.createApplicationClassLoader(mock(ApplicationDescriptor.class),
                                                                       mock(PluginClassLoaderResolver.class)));
    verifyClassLoaderRegistered(decorator.createApplicationClassLoader(mock(ApplicationDescriptor.class), mock(Supplier.class)));
    verifyClassLoaderRegistered(decorator.createApplicationClassLoader(mock(ApplicationDescriptor.class), mock(Supplier.class),
                                                                       mock(PluginClassLoaderResolver.class)));
    verifyClassLoaderRegistered(decorator.createMulePluginClassLoader(mock(ZernovaDeployableArtifactClassLoader.class),
                                                                      mock(ArtifactPluginDescriptor.class),
                                                                      mock(PluginDescriptorResolver.class)));
    verifyClassLoaderRegistered(decorator
        .createMulePluginClassLoader(mock(ZernovaDeployableArtifactClassLoader.class), mock(ArtifactPluginDescriptor.class),
                                     mock(PluginDescriptorResolver.class), mock(PluginClassLoaderResolver.class)));
  }

  private void verifyClassLoaderRegistered(ZernovaArtifactClassLoader classLoader) {
    verify(artifactClassLoaderManager).register(classLoader);
  }

  private void verifyClassLoaderRegistered(ZernovaDeployableArtifactClassLoader classLoader) {
    verify(artifactClassLoaderManager).register(classLoader);
    classLoader.getArtifactPluginClassLoaders().forEach(pcl -> verify(artifactClassLoaderManager).register(pcl));
  }

  @Test
  public void disposesClassLoaders() {
    verifyClassLoaderDisposed(decorator.createDomainClassLoader(mock(DomainDescriptor.class)));
    verifyClassLoaderDisposed(decorator.createDomainClassLoader(mock(DomainDescriptor.class),
                                                                mock(PluginClassLoaderResolver.class)));
    verifyClassLoaderDisposed(decorator.createApplicationClassLoader(mock(ApplicationDescriptor.class)));
    verifyClassLoaderDisposed(decorator.createApplicationClassLoader(mock(ApplicationDescriptor.class),
                                                                     mock(PluginClassLoaderResolver.class)));
    verifyClassLoaderDisposed(decorator.createApplicationClassLoader(mock(ApplicationDescriptor.class), mock(Supplier.class)));
    verifyClassLoaderDisposed(decorator.createApplicationClassLoader(mock(ApplicationDescriptor.class), mock(Supplier.class),
                                                                     mock(PluginClassLoaderResolver.class)));
    verifyClassLoaderDisposed(decorator.createMulePluginClassLoader(mock(ZernovaDeployableArtifactClassLoader.class),
                                                                    mock(ArtifactPluginDescriptor.class),
                                                                    mock(PluginDescriptorResolver.class)));
    verifyClassLoaderDisposed(decorator
        .createMulePluginClassLoader(mock(ZernovaDeployableArtifactClassLoader.class), mock(ArtifactPluginDescriptor.class),
                                     mock(PluginDescriptorResolver.class), mock(PluginClassLoaderResolver.class)));
  }

  private void verifyClassLoaderDisposed(ZernovaArtifactClassLoader classLoader) {
    classLoader.dispose();
    verify(artifactClassLoaderManager).unregister(classLoader.getArtifactId());
  }

  private void verifyClassLoaderDisposed(ZernovaDeployableArtifactClassLoader classLoader) {
    classLoader.dispose();
    verify(artifactClassLoaderManager).unregister(classLoader.getArtifactId());
    classLoader.getArtifactPluginClassLoaders().forEach(pcl -> {
      pcl.dispose();
      verify(artifactClassLoaderManager).unregister(pcl.getArtifactId());
    });
  }

}
