/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.internal.artifact;

import static com.zernova.runtime.core.internal.util.CompositeClassLoader.from;
import static com.zernova.runtime.deployment.model.internal.artifact.CompositeClassLoaderArtifactFinder.findClassLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.core.internal.util.CompositeClassLoader;
import com.zernova.runtime.deployment.model.api.application.ApplicationDescriptor;
import com.zernova.runtime.deployment.model.api.domain.DomainDescriptor;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaDeployableArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import com.zernova.tck.size.SmallTest;

import org.junit.Test;

@SmallTest
public class CompositeClassLoaderArtifactFinderTestCase {

  private static final ArtifactDescriptor pluginDescriptorOld = new ArtifactPluginDescriptor("my-plugin-old");
  private static final ArtifactDescriptor appDescriptorOld = new ApplicationDescriptor("my-app-old");
  private static final ArtifactDescriptor domainDescriptorOld = new DomainDescriptor("my-domain-old");

  private static final ArtifactDescriptor pluginDescriptor =
      new com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor("my-plugin");
  private static final ArtifactDescriptor appDescriptor =
      new com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor("my-app");
  private static final ArtifactDescriptor domainDescriptor =
      new com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor("my-domain");

  @Test
  public void appClassLoaderOld() {
    ClassLoader appClassLoader = mockArtifactClassLoader(appDescriptorOld);
    CompositeClassLoader compositeClassLoader = from(mockArtifactClassLoader(pluginDescriptorOld), appClassLoader);

    assertThat(findClassLoader(compositeClassLoader), equalTo(appClassLoader));
  }

  @Test
  public void domainClassLoaderOld() {
    ClassLoader domainClassLoader = mockArtifactClassLoader(domainDescriptorOld);
    CompositeClassLoader compositeClassLoader = from(mockArtifactClassLoader(pluginDescriptorOld), domainClassLoader);

    assertThat(findClassLoader(compositeClassLoader), equalTo(domainClassLoader));
  }

  @Test
  public void firstDelegateIfNoArtifactOldClassLoaderFound() {
    ClassLoader pluginClassLoader = mockArtifactClassLoader(pluginDescriptorOld);
    CompositeClassLoader compositeClassLoader = from(pluginClassLoader);

    assertThat(findClassLoader(compositeClassLoader), equalTo(pluginClassLoader));
  }

  @Test
  public void appClassLoader() {
    ClassLoader appClassLoader = mockArtifactClassLoader(appDescriptor);
    CompositeClassLoader compositeClassLoader = from(mockArtifactClassLoader(pluginDescriptor), appClassLoader);

    assertThat(findClassLoader(compositeClassLoader), equalTo(appClassLoader));
  }

  @Test
  public void domainClassLoader() {
    ClassLoader domainClassLoader = mockArtifactClassLoader(domainDescriptor);
    CompositeClassLoader compositeClassLoader = from(mockArtifactClassLoader(pluginDescriptor), domainClassLoader);

    assertThat(findClassLoader(compositeClassLoader), equalTo(domainClassLoader));
  }

  @Test
  public void firstDelegateIfNoArtifactClassLoaderFound() {
    ClassLoader pluginClassLoader = mockArtifactClassLoader(pluginDescriptor);
    CompositeClassLoader compositeClassLoader = from(pluginClassLoader);

    assertThat(findClassLoader(compositeClassLoader), equalTo(pluginClassLoader));
  }

  private static ClassLoader mockArtifactClassLoader(ArtifactDescriptor descriptor) {
    ZernovaDeployableArtifactClassLoader classLoader = mock(ZernovaDeployableArtifactClassLoader.class);
    when(classLoader.getArtifactDescriptor()).thenReturn(descriptor);
    return classLoader;
  }

}
