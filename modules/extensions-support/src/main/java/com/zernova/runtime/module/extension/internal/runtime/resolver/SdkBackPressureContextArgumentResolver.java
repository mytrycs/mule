/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.module.extension.internal.runtime.source.legacy.SdkBackPressureContextAdapter;
import com.zernova.sdk.api.runtime.source.BackPressureContext;

/**
 * {@link ArgumentResolver} which resolves to a {@link BackPressureContext} by delegating into a
 * {@link BackPressureContextArgumentResolver} and adapting the result.
 *
 * @since 4.5.0
 */
public class SdkBackPressureContextArgumentResolver implements ArgumentResolver<BackPressureContext> {

  private final ArgumentResolver<com.zernova.runtime.extension.api.runtime.source.BackPressureContext> backPressureContextArgumentResolver =
      new BackPressureContextArgumentResolver();

  @Override
  public BackPressureContext resolve(ExecutionContext executionContext) {
    com.zernova.runtime.extension.api.runtime.source.BackPressureContext backPressureContext =
        backPressureContextArgumentResolver.resolve(executionContext);
    return backPressureContext == null ? null : new SdkBackPressureContextAdapter(backPressureContext);
  }
}
