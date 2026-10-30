/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static com.zernova.runtime.extension.api.runtime.source.SourceResult.invocationError;
import static com.zernova.runtime.extension.api.runtime.source.SourceResult.responseError;
import static com.zernova.runtime.extension.api.runtime.source.SourceResult.success;

import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext;
import com.zernova.runtime.extension.api.runtime.source.SourceResult;

/**
 * {@link ArgumentResolver} implementation which create instances of {@link SourceResult}
 *
 * @since 4.0
 */
public class SourceResultArgumentResolver extends AbstractSourceResultArgumentResolver<SourceResult> {

  private final ArgumentResolver<Error> errorArgumentResolver;
  private final ArgumentResolver<SourceCallbackContext> callbackContextArgumentResolver;

  public SourceResultArgumentResolver(ArgumentResolver<Error> errorArgumentResolver,
                                      ArgumentResolver<SourceCallbackContext> callbackContextArgumentResolver) {
    super(errorArgumentResolver);
    this.errorArgumentResolver = errorArgumentResolver;
    this.callbackContextArgumentResolver = callbackContextArgumentResolver;
  }

  @Override
  protected SourceResult resolveSuccess(ExecutionContext executionContext) {
    return success(callbackContextArgumentResolver.resolve(executionContext));
  }

  @Override
  protected SourceResult resolveInvocationError(ExecutionContext executionContext) {
    return invocationError(errorArgumentResolver.resolve(executionContext),
                           callbackContextArgumentResolver.resolve(executionContext));
  }

  @Override
  protected SourceResult resolveResponseError(ExecutionContext executionContext) {
    return responseError(errorArgumentResolver.resolve(executionContext),
                         callbackContextArgumentResolver.resolve(executionContext));
  }
}
