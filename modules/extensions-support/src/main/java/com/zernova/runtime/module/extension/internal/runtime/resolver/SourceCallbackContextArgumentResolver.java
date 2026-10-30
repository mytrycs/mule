/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static com.zernova.runtime.module.extension.internal.ExtensionProperties.SOURCE_CALLBACK_CONTEXT_PARAM;

import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.internal.ExtensionProperties;
import com.zernova.sdk.api.runtime.source.SourceCallbackContext;

/**
 * An {@link ArgumentResolver} which returns a {@link com.zernova.sdk.api.runtime.source.SourceCallbackContext} registered as an
 * {@link ExecutionContext} variable under the key {@link ExtensionProperties#SOURCE_CALLBACK_CONTEXT_PARAM}.
 * <p>
 * If no such variable exists, then it returns {@code null}
 *
 * @since 4.0
 */
public class SourceCallbackContextArgumentResolver implements ArgumentResolver<SourceCallbackContext> {

  @Override
  public SourceCallbackContext resolve(ExecutionContext executionContext) {
    return ((ExecutionContextAdapter<ComponentModel>) executionContext).getVariable(SOURCE_CALLBACK_CONTEXT_PARAM);
  }
}
