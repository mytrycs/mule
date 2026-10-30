/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.service.api.artifact;

import com.zernova.runtime.container.api.ContainerDependantArtifactClassLoaderFactory;
import com.zernova.runtime.container.api.ZernovaContainerClassLoaderWrapper;
import com.zernova.runtime.container.internal.DefaultZernovaContainerClassLoaderWrapper;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoaderFactory;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import com.zernova.runtime.module.artifact.api.classloader.exception.ArtifactClassloaderCreationException;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;

public interface IServiceClassLoaderFactory
    extends ArtifactClassLoaderFactory<ServiceDescriptor>, ContainerDependantArtifactClassLoaderFactory<ServiceDescriptor> {

  @Deprecated(since = "4.10.0")
  /**
   * @deprecated use {@link #create(String, ArtifactDescriptor, ZernovaContainerClassLoaderWrapper)} or
   *             {@link #create(String, ServiceDescriptor, ArtifactClassLoader)} instead.
   */
  ArtifactClassLoader create(String artifactId, ServiceDescriptor descriptor, ClassLoader parent,
                             ClassLoaderLookupPolicy lookupPolicy)
      throws ArtifactClassloaderCreationException;

  /**
   * Creates a {@link ClassLoader} from a given descriptor.
   *
   * @param artifactId           artifact unique ID.
   * @param descriptor           descriptor of the artifact owner of the created class loader.
   * @param containerClassLoader parent for the new artifact class loader.
   * @return a new class loader for described artifact.
   *
   * @since 4.6
   */
  default ArtifactClassLoader create(String artifactId, ServiceDescriptor descriptor, ArtifactClassLoader containerClassLoader)
      throws ArtifactClassloaderCreationException {
    return create(artifactId, descriptor, new DefaultZernovaContainerClassLoaderWrapper(containerClassLoader));
  }
}
