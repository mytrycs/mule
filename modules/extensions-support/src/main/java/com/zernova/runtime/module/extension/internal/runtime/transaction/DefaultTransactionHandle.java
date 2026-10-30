/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.transaction;

import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.core.api.transaction.Transaction;
import com.zernova.runtime.core.api.transaction.TransactionCoordination;
import com.zernova.runtime.extension.api.tx.TransactionHandle;

/**
 * Default {@link TransactionHandle} implementation.
 * <p>
 * This will execute a transaction action only is the transaction is still alive.
 *
 * @since 4.0
 */
public class DefaultTransactionHandle implements TransactionHandle {

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isTransacted() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void commit() throws TransactionException {
    Transaction transaction = TransactionCoordination.getInstance().getTransaction();
    if (transaction != null) {
      transaction.commit();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void rollback() throws TransactionException {
    Transaction transaction = TransactionCoordination.getInstance().getTransaction();
    if (transaction != null) {
      transaction.rollback();
    }
  }

  @Override
  public void resolve() throws TransactionException {
    Transaction transaction = TransactionCoordination.getInstance().getTransaction();
    if (transaction != null) {
      TransactionCoordination.getInstance().resolveTransaction();
    }
  }
}
