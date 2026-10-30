/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.container.api;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;

/**
 * Represents a container class loader.
 *
 * @since 4.5
 */
@NoImplement
public interface ZernovaContainerClassLoaderWrapper {

  /**
   * @return the filtered class loader encapsulating the internals of the container class loader.
   */
  ArtifactClassLoader getContainerClassLoader();

  /**
   * @return the container class loader lookup policy.
   */
  ClassLoaderLookupPolicy getContainerClassLoaderLookupPolicy();
}
