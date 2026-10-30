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
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.internal.runtime.execution.adapter.SdkCompletionCallbackAdapter;
import com.zernova.sdk.api.runtime.process.CompletionCallback;

/**
 * {@link ArgumentResolver} which resolves a {@link CompletionCallback} by adapting the content of the {@link ExecutionContext}
 * variable
 * {@link com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextProperties#COMPLETION_CALLBACK_CONTEXT_PARAM}
 *
 * @since 4.5.0
 */
public final class SdkCompletionCallbackArgumentResolver implements ArgumentResolver<CompletionCallback> {

  @Override
  public CompletionCallback resolve(ExecutionContext executionContext) {
    com.zernova.runtime.extension.api.runtime.process.CompletionCallback completionCallback =
        (com.zernova.runtime.extension.api.runtime.process.CompletionCallback) ((ExecutionContextAdapter) executionContext)
            .getVariable(COMPLETION_CALLBACK_CONTEXT_PARAM);
    return completionCallback == null ? null : new SdkCompletionCallbackAdapter(completionCallback);
  }
}
