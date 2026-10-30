/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.extension.api.security.AuthenticationHandler;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.internal.runtime.security.DefaultAuthenticationHandler;

/**
 * An {@link ArgumentResolver} which returns the {@link AuthenticationHandler} of the current event
 *
 * @since 4.0
 */
public final class SecurityContextHandlerArgumentResolver implements ArgumentResolver<AuthenticationHandler> {

  @Override
  public AuthenticationHandler resolve(ExecutionContext executionContext) {
    ExecutionContextAdapter context = ((ExecutionContextAdapter) executionContext);
    return new DefaultAuthenticationHandler(context.getSecurityContext(),
                                            context.getSecurityManager(),
                                            context::setSecurityContext);
  }
}
