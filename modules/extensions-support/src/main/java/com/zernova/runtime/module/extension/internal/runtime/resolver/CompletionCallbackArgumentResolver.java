/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextProperties.COMPLETION_CALLBACK_CONTEXT_PARAM;

import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.extension.api.runtime.process.CompletionCallback;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;

/**
 * {@link ArgumentResolver} which returns the
 * {@link com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextProperties#COMPLETION_CALLBACK_CONTEXT_PARAM}
 * context variable.
 * <p/>
 * Notice that this resolver only works if the {@link ExecutionContext} is a {@link ExecutionContextAdapter}
 *
 * @since 4.0
 */
public final class CompletionCallbackArgumentResolver implements ArgumentResolver<CompletionCallback> {

  @Override
  public CompletionCallback resolve(ExecutionContext executionContext) {
    return (CompletionCallback) ((ExecutionContextAdapter) executionContext).getVariable(COMPLETION_CALLBACK_CONTEXT_PARAM);
  }
}
