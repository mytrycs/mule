/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.internal.application;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.module.artifact.api.classloader.ParentFirstLookupStrategy.PARENT_FIRST;

import com.zernova.runtime.module.artifact.activation.internal.classloader.ZernovaApplicationClassLoader;
import com.zernova.runtime.module.artifact.activation.internal.nativelib.DefaultNativeLibraryFinderFactory;
import com.zernova.runtime.module.artifact.activation.internal.nativelib.NativeLibraryFinder;
import com.zernova.runtime.module.artifact.activation.internal.nativelib.NativeLibraryFinderFactory;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import com.zernova.runtime.module.artifact.api.classloader.DeployableArtifactClassLoaderFactory;
import com.zernova.runtime.module.artifact.api.classloader.LookupStrategy;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Creates {@link ZernovaApplicationClassLoader} instances based on the application descriptor.
 */
public class ZernovaApplicationClassLoaderFactory implements DeployableArtifactClassLoaderFactory<ApplicationDescriptor> {

  private final NativeLibraryFinderFactory nativeLibraryFinderFactory;

  /**
   * Creates a new factory
   *
   * @param nativeLibraryFinderFactory creates {@link NativeLibraryFinder} for the created applications. Non null
   */
  public ZernovaApplicationClassLoaderFactory(NativeLibraryFinderFactory nativeLibraryFinderFactory) {

    checkArgument(nativeLibraryFinderFactory != null, "nativeLibraryFinderFactory cannot be null");
    this.nativeLibraryFinderFactory = nativeLibraryFinderFactory;
  }

  /**
   * Creates a new factory
   *
   * @param nativeLibsTempFolderChildFunction a function to determine the location of a temp dir to copy the native libs of the
   *                                          artifact to, based on the deployment name.
   */
  public ZernovaApplicationClassLoaderFactory(Function<String, File> nativeLibsTempFolderChildFunction) {
    this.nativeLibraryFinderFactory = new DefaultNativeLibraryFinderFactory(nativeLibsTempFolderChildFunction);
  }

  @Override
  public ArtifactClassLoader create(String artifactId, ArtifactClassLoader parent, ApplicationDescriptor descriptor) {
    final ClassLoaderLookupPolicy classLoaderLookupPolicy = getApplicationClassLoaderLookupPolicy(parent, descriptor);

    return new ZernovaApplicationClassLoader(artifactId, descriptor, parent.getClassLoader(),
                                          nativeLibraryFinderFactory.create(descriptor.getDataFolderName(),
                                                                            descriptor.getLoadedNativeLibrariesFolderName(),
                                                                            descriptor.getClassLoaderConfiguration().getUrls()),
                                          Arrays.asList(descriptor.getClassLoaderConfiguration().getUrls()),
                                          classLoaderLookupPolicy);
  }

  @Override
  public ArtifactClassLoader create(String artifactId, ArtifactClassLoader parent, ApplicationDescriptor descriptor,
                                    List<ArtifactClassLoader> artifactPluginClassLoaders) {
    return create(artifactId, parent, descriptor);
  }

  private ClassLoaderLookupPolicy getApplicationClassLoaderLookupPolicy(ArtifactClassLoader parent,
                                                                        ApplicationDescriptor descriptor) {

    final Map<String, LookupStrategy> pluginsLookupStrategies = new HashMap<>();

    for (ArtifactPluginDescriptor artifactPluginDescriptor : descriptor.getPlugins()) {
      artifactPluginDescriptor.getClassLoaderConfiguration().getExportedPackages()
          .forEach(p -> pluginsLookupStrategies.put(p, PARENT_FIRST));
    }

    return parent.getClassLoaderLookupPolicy().extend(pluginsLookupStrategies);
  }


}
