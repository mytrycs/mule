/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.module.extension.internal.runtime.execution.adapter.SdkRouterCompletionCallback;
import com.zernova.sdk.api.runtime.process.RouterCompletionCallback;

/**
 * {@link ArgumentResolver} which returns a {@link RouterCompletionCallback}
 *
 * @since 4.5.0
 */
public final class SdkRouterCallbackArgumentResolver implements ArgumentResolver<RouterCompletionCallback> {

  private final ArgumentResolver<com.zernova.runtime.extension.api.runtime.process.RouterCompletionCallback> routerCallbackArgumentResolver =
      new RouterCallbackArgumentResolver();

  @Override
  public RouterCompletionCallback resolve(ExecutionContext executionContext) {
    com.zernova.runtime.extension.api.runtime.process.RouterCompletionCallback routerCompletionCallback =
        routerCallbackArgumentResolver.resolve(executionContext);
    return routerCompletionCallback == null ? null : new SdkRouterCompletionCallback(routerCompletionCallback);
  }
}
