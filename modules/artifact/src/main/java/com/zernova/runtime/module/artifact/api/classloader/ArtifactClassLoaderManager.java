/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.api.classloader;

import com.zernova.api.annotation.NoImplement;

/**
 * Tracks {@link ArtifactClassLoader} instances created on the container.
 */
@NoImplement
public interface ArtifactClassLoaderManager {

  /**
   * Registers a new class loader
   *
   * @param artifactClassLoader created classloader. Non null
   */
  void register(ArtifactClassLoader artifactClassLoader);

  /**
   * Un-registers a disposed class loader.
   *
   * @param classLoaderId identifier for the classloader to be unregistered. Non empty.
   * @return the classloader registered under the given ID, null if no class loader with that ID was registered before.
   */
  ArtifactClassLoader unregister(String classLoaderId);
}
