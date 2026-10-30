/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.module.extension.internal.runtime.security.adapter.SdkAuthenticationHandlerAdapter;
import com.zernova.sdk.api.security.AuthenticationHandler;

/**
 * {@link ArgumentResolver} which resolves to a {@link AuthenticationHandler} by delegating into a
 * {@link SecurityContextHandlerArgumentResolver} and adapting the result.
 *
 * @since 4.5.0
 */
public final class SdkSecurityContextHandlerArgumentResolver implements ArgumentResolver<AuthenticationHandler> {

  private final ArgumentResolver<com.zernova.runtime.extension.api.security.AuthenticationHandler> authenticationHandlerArgumentResolver =
      new SecurityContextHandlerArgumentResolver();

  @Override
  public AuthenticationHandler resolve(ExecutionContext executionContext) {
    com.zernova.runtime.extension.api.security.AuthenticationHandler authenticationHandler =
        authenticationHandlerArgumentResolver.resolve(executionContext);
    return authenticationHandler == null ? null : new SdkAuthenticationHandlerAdapter(authenticationHandler);
  }
}
