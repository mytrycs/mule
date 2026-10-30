/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.loader.xml;

import static java.util.Collections.singleton;

import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoaderProvider;
import com.zernova.runtime.extension.internal.loader.xml.XmlExtensionModelLoader;

import java.util.Set;

public class XmlExtensionModelLoaderProvider implements ExtensionModelLoaderProvider {

  @Override
  public Set<ExtensionModelLoader> getExtensionModelLoaders() {
    return singleton(new XmlExtensionModelLoader());
  }
}
