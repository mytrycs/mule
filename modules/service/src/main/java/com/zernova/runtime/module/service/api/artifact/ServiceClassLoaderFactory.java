/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.service.api.artifact;

import com.zernova.runtime.container.api.ZernovaContainerClassLoaderWrapper;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.exception.ArtifactClassloaderCreationException;

/**
 * Creates {@link ArtifactClassLoader} for service descriptors.
 *
 * @deprecated from 4.9 use {@link ServiceModuleLayerFactory}
 */
// TODO W-12780081 - remove usages of deprecated creation metho
@Deprecated(since = "4.9")
public class ServiceClassLoaderFactory
    implements IServiceClassLoaderFactory {

  /**
   * @deprecated from 4.6 use {@link ServiceClassLoaderFactoryProvider} instead.
   */
  @Deprecated(since = "4.6")
  public ServiceClassLoaderFactory() {
    // Nothing to do
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated since 4.6, use {@link #create(String, ServiceDescriptor, ZernovaContainerClassLoaderWrapper)}.
   */
  @Override
  @Deprecated(since = "4.6")
  public ArtifactClassLoader create(String artifactId, ServiceDescriptor descriptor, ClassLoader parent,
                                    ClassLoaderLookupPolicy lookupPolicy)
      throws ArtifactClassloaderCreationException {
    return new ZernovaArtifactClassLoader(artifactId, descriptor, descriptor.getClassLoaderConfiguration().getUrls(), parent,
                                       lookupPolicy);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ArtifactClassLoader create(String artifactId, ServiceDescriptor descriptor,
                                    ZernovaContainerClassLoaderWrapper containerClassLoader)
      throws ArtifactClassloaderCreationException {
    return new ZernovaArtifactClassLoader(artifactId, descriptor, descriptor.getClassLoaderConfiguration().getUrls(),
                                       containerClassLoader.getContainerClassLoader().getClassLoader(),
                                       containerClassLoader.getContainerClassLoaderLookupPolicy());
  }

  @Override
  public void setParentLayerFrom(Class clazz) {
    // Nothing to do
  }

}
