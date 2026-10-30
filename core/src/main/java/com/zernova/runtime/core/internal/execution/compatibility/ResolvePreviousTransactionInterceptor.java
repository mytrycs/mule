/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.execution.compatibility;

import com.zernova.runtime.core.api.execution.ExecutionCallback;
import com.zernova.runtime.core.api.transaction.Transaction;
import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.core.api.transaction.TransactionCoordination;
import com.zernova.runtime.core.internal.execution.ExecutionContext;
import com.zernova.runtime.core.internal.execution.ExecutionInterceptor;
import com.zernova.runtime.core.privileged.transaction.TransactionConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ExecutionInterceptor} implementation to exclusively be used for Compatibility purposes.
 */
public class ResolvePreviousTransactionInterceptor<T> implements ExecutionInterceptor<T> {

  private static final Logger logger = LoggerFactory.getLogger(ResolvePreviousTransactionInterceptor.class);
  final private ExecutionInterceptor<T> next;
  private TransactionConfig transactionConfig;

  public ResolvePreviousTransactionInterceptor(ExecutionInterceptor<T> next, TransactionConfig transactionConfig) {
    this.next = next;
    this.transactionConfig = transactionConfig;
  }

  @Override
  public T execute(ExecutionCallback<T> callback, ExecutionContext executionContext) throws Exception {
    byte action = transactionConfig.getAction();
    Transaction transactionBeforeTemplate = TransactionCoordination.getInstance().getTransaction();
    if ((action == TransactionConfig.ACTION_NONE || action == TransactionConfig.ACTION_ALWAYS_BEGIN)
        && transactionBeforeTemplate != null) {
      if (logger.isDebugEnabled()) {
        logger.debug(action + ", " + "current TX: " + transactionBeforeTemplate);
      }

      resolveTransaction();
    }
    return next.execute(callback, executionContext);
  }

  protected void resolveTransaction() throws TransactionException {
    TransactionCoordination.getInstance().resolveTransaction();
  }
}
