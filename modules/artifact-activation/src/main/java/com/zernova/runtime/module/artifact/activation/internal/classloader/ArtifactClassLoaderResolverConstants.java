/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.internal.classloader;

import static com.zernova.runtime.container.api.ContainerClassLoaderProvider.createContainerClassLoader;

import com.zernova.runtime.container.api.ModuleRepository;
import com.zernova.runtime.container.internal.ContainerModuleDiscoverer;
import com.zernova.runtime.container.internal.DefaultModuleRepository;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;

/**
 * Provides constants to be used internally for the generation of classloaders
 *
 * @since 4.5
 */
public class ArtifactClassLoaderResolverConstants {

  /**
   * A {@link ModuleRepository} for the modules available in the same classpath as this class.
   */
  public static final ModuleRepository MODULE_REPOSITORY =
      new DefaultModuleRepository(new ContainerModuleDiscoverer());

  /**
   * An {@link ArtifactClassLoader} for the Mule Container with the modules available in {@link #MODULE_REPOSITORY}.
   */
  public static final ArtifactClassLoader CONTAINER_CLASS_LOADER =
      createContainerClassLoader(MODULE_REPOSITORY);

}
