/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tls.api.extension;

import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.getTlsExtensionModel;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.extension.api.provider.RuntimeExtensionModelProvider;

public final class TlsRuntimeExtensionModelProvider implements RuntimeExtensionModelProvider {

  @Override
  public ExtensionModel createExtensionModel() {
    return getTlsExtensionModel();
  }
}
