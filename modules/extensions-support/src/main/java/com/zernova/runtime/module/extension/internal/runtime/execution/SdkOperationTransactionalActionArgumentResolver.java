/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.execution;

import static com.zernova.runtime.extension.api.ExtensionConstants.TRANSACTIONAL_ACTION_PARAMETER_NAME;

import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ArgumentResolver;
import com.zernova.sdk.api.tx.OperationTransactionalAction;

/**
 * {@link ArgumentResolver} implementation for {@link OperationTransactionalAction} parameters which delegates to a
 * {@link OperationTransactionalActionArgumentResolver} and adapts the result.
 *
 * @since 4.5
 */
public class SdkOperationTransactionalActionArgumentResolver implements ArgumentResolver<OperationTransactionalAction> {

  /**
   * {@inheritDoc}
   */
  @Override
  public OperationTransactionalAction resolve(ExecutionContext executionContext) {
    return (OperationTransactionalAction) executionContext.getParameter(TRANSACTIONAL_ACTION_PARAMETER_NAME);
  }
}
