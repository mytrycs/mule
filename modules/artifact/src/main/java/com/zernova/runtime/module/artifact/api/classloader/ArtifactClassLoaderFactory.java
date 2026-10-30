/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.api.classloader;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.module.artifact.api.classloader.exception.ArtifactClassloaderCreationException;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;

/**
 * Creates {@link ClassLoader} instances for Mule applications
 */
@NoImplement
public interface ArtifactClassLoaderFactory<T extends ArtifactDescriptor> {

  /**
   * Creates a classLoader from a given descriptor
   *
   * @param artifactId   artifact unique ID
   * @param descriptor   descriptor of the artifact owner of the created classloader
   * @param parent       parent for the new artifact classloader.
   * @param lookupPolicy lookup policy to use on the created classloader.
   * @return a new classLoader for described artifact
   */
  ArtifactClassLoader create(String artifactId, T descriptor, ClassLoader parent, ClassLoaderLookupPolicy lookupPolicy)
      throws ArtifactClassloaderCreationException;

}
