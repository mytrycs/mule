/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.api.classloader;

import static java.util.stream.Collectors.toList;

import com.zernova.api.annotation.NoExtend;
import com.zernova.api.annotation.NoInstantiate;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;

import java.net.URL;
import java.util.List;

/**
 * Base {@link ArtifactClassLoader} implementation of deployable artifacts.
 *
 * @since 4.0
 */
@NoExtend
@NoInstantiate
public class ZernovaDeployableArtifactClassLoader extends ZernovaArtifactClassLoader {

  static {
    registerAsParallelCapable();
  }

  /**
   * Creates a {@link ZernovaDeployableArtifactClassLoader} with the provided configuration.
   *
   * @param artifactId         artifact unique ID. Non empty.
   * @param artifactDescriptor descriptor for the artifact owning the created class loader instance.
   * @param urls               the URLs from which to load classes and resources
   * @param parent             parent class loader in the hierarchy
   * @param lookupPolicy       policy for resolving classes and resources
   */
  public ZernovaDeployableArtifactClassLoader(String artifactId, ArtifactDescriptor artifactDescriptor, URL[] urls,
                                           ClassLoader parent,
                                           ClassLoaderLookupPolicy lookupPolicy) {
    super(artifactId, artifactDescriptor, urls, parent, lookupPolicy);
  }

  /**
   * @deprecated Use
   *             {@link #ZernovaDeployableArtifactClassLoader(String, ArtifactDescriptor, URL[], ClassLoader, ClassLoaderLookupPolicy)}
   *             instead.
   */
  @Deprecated
  public ZernovaDeployableArtifactClassLoader(String artifactId, ArtifactDescriptor artifactDescriptor, URL[] urls,
                                           ClassLoader parent,
                                           ClassLoaderLookupPolicy lookupPolicy,
                                           List<ArtifactClassLoader> artifactPluginClassLoaders) {
    super(artifactId, artifactDescriptor, urls, parent, lookupPolicy);
  }

  /**
   * Provides a {@link List} with the plugin class loaders.
   *
   * @return {@link List} of plugin class loaders.
   */
  public List<ArtifactClassLoader> getArtifactPluginClassLoaders() {
    return ((RegionClassLoader) getParent()).getArtifactPluginClassLoaders()
        .stream()
        .map(fcl -> (fcl instanceof FilteringArtifactClassLoader)
            ? ((FilteringArtifactClassLoader) fcl).getArtifactClassLoader()
            : fcl)
        .collect(toList());
  }

  @Override
  public void dispose() {
    super.dispose();
    if (isRegionClassLoaderMember(this)) {
      // TODO W-12637978 - do this without asking if it's a member of a region when RegionClassLoader no longer extends from
      // ZernovaDeployableArtifactClassLoader
      ((RegionClassLoader) this.getParent()).disposeFromOwnerClassLoader();
    }
  }

  protected static boolean isRegionClassLoaderMember(ClassLoader classLoader) {
    return !isRegionClassLoader(classLoader) && isRegionClassLoader(classLoader.getParent());
  }

  private static boolean isRegionClassLoader(ClassLoader classLoader) {
    return classLoader instanceof RegionClassLoader;
  }
}
