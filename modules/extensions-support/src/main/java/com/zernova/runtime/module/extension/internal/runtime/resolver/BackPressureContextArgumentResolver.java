/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static com.zernova.runtime.extension.api.runtime.source.BackPressureAction.FAIL;
import static com.zernova.runtime.module.extension.internal.ExtensionProperties.BACK_PRESSURE_ACTION_CONTEXT_PARAM;

import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.extension.api.runtime.source.BackPressureAction;
import com.zernova.runtime.extension.api.runtime.source.BackPressureContext;
import com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.internal.runtime.source.ImmutableBackPressureContext;

/**
 * Resolves an instance of {@link BackPressureContext}
 *
 * @since 1.1
 */
public class BackPressureContextArgumentResolver implements ArgumentResolver<BackPressureContext> {

  private final ArgumentResolver<SourceCallbackContext> callbackContextResolver =
      new LegacySourceCallbackContextArgumentResolver();

  @Override
  public BackPressureContext resolve(ExecutionContext executionContext) {
    ExecutionContextAdapter ctx = (ExecutionContextAdapter) executionContext;
    BackPressureAction action = (BackPressureAction) ctx.getVariable(BACK_PRESSURE_ACTION_CONTEXT_PARAM);
    if (action == null) {
      action = FAIL;
    }

    return new ImmutableBackPressureContext(ctx.getEvent(), action, callbackContextResolver.resolve(ctx));
  }
}
