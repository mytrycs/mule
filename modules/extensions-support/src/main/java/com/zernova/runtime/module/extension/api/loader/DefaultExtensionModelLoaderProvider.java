/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.api.loader;

import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoaderProvider;
import com.zernova.runtime.module.extension.internal.loader.java.CraftedExtensionModelLoader;
import com.zernova.runtime.module.extension.internal.loader.java.DefaultJavaExtensionModelLoader;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link ExtensionModelLoaderProvider} which yields the main {@link ExtensionModelLoader} instances
 *
 * @since 4.5.0
 */
public class DefaultExtensionModelLoaderProvider implements ExtensionModelLoaderProvider {

  @Override
  public Set<ExtensionModelLoader> getExtensionModelLoaders() {
    Set<ExtensionModelLoader> loaders = new HashSet<>();
    loaders.add(new DefaultJavaExtensionModelLoader());
    loaders.add(new CraftedExtensionModelLoader());

    return loaders;
  }
}
