/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.api.extension;


import static com.zernova.runtime.api.util.ZernovaSystemProperties.ENABLE_MULE_SDK_PROPERTY;
import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.getOperationDslExtensionModel;

import static java.lang.Boolean.getBoolean;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.extension.api.provider.RuntimeExtensionModelProvider;

/**
 * Provides the {@link ExtensionModel} for operations Mule DSL
 *
 * @since 4.5.0
 */
public final class OperationDslExtensionModelProvider implements RuntimeExtensionModelProvider {

  private final boolean isMuleSdkEnabled = getBoolean(ENABLE_MULE_SDK_PROPERTY);

  @Override
  public ExtensionModel createExtensionModel() {
    return isMuleSdkEnabled ? getOperationDslExtensionModel() : null;
  }
}
