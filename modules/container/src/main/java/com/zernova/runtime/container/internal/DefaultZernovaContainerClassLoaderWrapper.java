/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.container.internal;

import com.zernova.runtime.container.api.ZernovaContainerClassLoaderWrapper;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;

/**
 * Default implementation of {@link ZernovaContainerClassLoaderWrapper}.
 *
 * @since 4.5
 */
public class DefaultZernovaContainerClassLoaderWrapper implements ZernovaContainerClassLoaderWrapper {

  private final ArtifactClassLoader containerClassLoader;

  public DefaultZernovaContainerClassLoaderWrapper(ArtifactClassLoader containerClassLoader) {
    this.containerClassLoader = containerClassLoader;
  }

  @Override
  public ArtifactClassLoader getContainerClassLoader() {
    return containerClassLoader;
  }

  @Override
  public ClassLoaderLookupPolicy getContainerClassLoaderLookupPolicy() {
    return containerClassLoader.getClassLoaderLookupPolicy();
  }
}
