/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.execution;

import com.zernova.runtime.core.api.execution.ExecutionCallback;
import com.zernova.runtime.core.api.transaction.Transaction;
import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.core.api.transaction.TransactionCoordination;
import com.zernova.runtime.core.privileged.exception.MessagingException;
import com.zernova.runtime.core.privileged.transaction.TransactionConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuspendXaTransactionInterceptor<T> implements ExecutionInterceptor<T> {

  private static final Logger logger = LoggerFactory.getLogger(SuspendXaTransactionInterceptor.class);
  private final ExecutionInterceptor<T> next;
  private final TransactionConfig transactionConfig;
  private final boolean processOnException;

  public SuspendXaTransactionInterceptor(ExecutionInterceptor<T> next, TransactionConfig transactionConfig,
                                         boolean processOnException) {
    this.next = next;
    this.transactionConfig = transactionConfig;
    this.processOnException = processOnException;
  }

  @Override
  public T execute(ExecutionCallback<T> callback, ExecutionContext executionContext) throws Exception {
    Transaction suspendedXATx = null;
    Transaction tx = TransactionCoordination.getInstance().getTransaction();
    byte action = transactionConfig.getAction();
    if ((action == TransactionConfig.ACTION_NONE || action == TransactionConfig.ACTION_ALWAYS_BEGIN) && tx != null && tx.isXA()) {
      if (logger.isDebugEnabled()) {
        logger.debug("suspending XA tx " + action + ", " + "current TX: " + tx);
      }
      suspendedXATx = tx;
      suspendXATransaction(suspendedXATx);
    }
    try {
      T result = next.execute(callback, executionContext);
      resumeXaTransactionIfRequired(suspendedXATx);
      return result;
    } catch (MessagingException e) {
      if (processOnException) {
        TransactionCoordination.getInstance().resumeXaTransactionIfAvailable();
      }
      throw e;
    }
  }

  protected void suspendXATransaction(Transaction tx) throws TransactionException {
    TransactionCoordination.getInstance().suspendCurrentTransaction();
  }

  protected void resumeXATransaction(Transaction tx) throws TransactionException {
    TransactionCoordination.getInstance().resumeSuspendedTransaction();
  }

  private void resumeXaTransactionIfRequired(Transaction suspendedXATx) throws TransactionException {
    if (suspendedXATx != null) {
      resumeXATransaction(suspendedXATx);
    }
  }

}

