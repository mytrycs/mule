/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transaction;

import static com.zernova.runtime.api.tx.TransactionType.LOCAL;

import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.api.tx.TransactionType;
import com.zernova.runtime.core.api.transaction.Transaction;

/**
 * Transaction factory for DelegateTransaction. Used for transactional element since transaction type is not known until the first
 * transactional message processor is executed.
 */
public final class DelegateTransactionFactory implements TypedTransactionFactory {

  @Override
  public Transaction beginTransaction(String applicationName, NotificationDispatcher notificationFirer)
      throws TransactionException {
    DelegateTransaction delegateTransaction =
        new DelegateTransaction(applicationName, notificationFirer);
    delegateTransaction.begin();
    return delegateTransaction;
  }

  @Override
  public boolean isTransacted() {
    return true;
  }

  @Override
  public TransactionType getType() {
    return LOCAL;
  }

}
