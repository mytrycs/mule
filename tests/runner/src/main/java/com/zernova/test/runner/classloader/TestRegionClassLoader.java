/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.runner.classloader;

import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import com.zernova.runtime.module.artifact.api.classloader.DefaultArtifactClassLoaderFilter;
import com.zernova.runtime.module.artifact.api.classloader.RegionClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;

import java.util.Set;

/**
 * {@link RegionClassLoader} specialization that provides a simpler api to add classloaders to the region.
 *
 * @since 4.2
 */
public final class TestRegionClassLoader extends RegionClassLoader {

  static {
    registerAsParallelCapable();
  }

  public TestRegionClassLoader(ClassLoader parent, ClassLoaderLookupPolicy lookupPolicy) {
    super("Region", new ArtifactDescriptor("Region"), parent, lookupPolicy);
  }

  /**
   * Adds a class loader to the region.
   *
   * @see RegionClassLoader#addClassLoader(com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader,
   *      com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoaderFilter)
   */
  public void addContent(ClassLoader parentClassLoader, Set<String> exportedClassPackages,
                         Set<String> exportedResources) {
    addClassLoader(new DelegatingArtifactClassLoader(parentClassLoader),
                   new DefaultArtifactClassLoaderFilter(exportedClassPackages, exportedResources));
  }
}
