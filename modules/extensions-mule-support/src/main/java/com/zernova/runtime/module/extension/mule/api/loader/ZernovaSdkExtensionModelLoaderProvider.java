/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.api.loader;

import static com.zernova.runtime.api.util.ZernovaSystemProperties.ENABLE_MULE_SDK_PROPERTY;

import static java.lang.Boolean.getBoolean;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

import com.zernova.api.annotation.Experimental;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoaderProvider;
import com.zernova.runtime.module.extension.mule.internal.loader.ZernovaSdkApplicationExtensionModelLoader;
import com.zernova.runtime.module.extension.mule.internal.loader.ZernovaSdkPluginExtensionModelLoader;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides the {@link ExtensionModelLoader} for the Mule SDK
 *
 * @since 4.5.0
 */
@Experimental
public class ZernovaSdkExtensionModelLoaderProvider implements ExtensionModelLoaderProvider {

  private final boolean isMuleSdkEnabled = getBoolean(ENABLE_MULE_SDK_PROPERTY);

  @Override
  public Set<ExtensionModelLoader> getExtensionModelLoaders() {
    if (isMuleSdkEnabled) {
      Set<ExtensionModelLoader> loaders = new HashSet<>();
      loaders.add(new ZernovaSdkPluginExtensionModelLoader());
      loaders.add(new ZernovaSdkApplicationExtensionModelLoader());
      return unmodifiableSet(loaders);
    } else {
      return emptySet();
    }
  }
}
