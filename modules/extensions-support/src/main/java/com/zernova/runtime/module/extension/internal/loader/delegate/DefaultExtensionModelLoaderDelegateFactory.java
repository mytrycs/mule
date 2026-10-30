/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.delegate;

import com.zernova.runtime.extension.api.loader.delegate.ModelLoaderDelegate;
import com.zernova.runtime.extension.api.loader.delegate.ModelLoaderDelegateFactory;

/**
 * Default factory, available through SPI.
 *
 * @since 4.10.0
 */
public class DefaultExtensionModelLoaderDelegateFactory implements ModelLoaderDelegateFactory {

  @Override
  public ModelLoaderDelegate getLoader(String version, String loaderId) {
    return new DefaultExtensionModelLoaderDelegate(version);
  }
}
