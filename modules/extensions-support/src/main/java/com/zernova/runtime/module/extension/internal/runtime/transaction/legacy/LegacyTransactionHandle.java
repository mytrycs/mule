/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.transaction.legacy;

import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.extension.api.tx.TransactionHandle;

/**
 * Adapts a {@link com.zernova.sdk.api.tx.TransactionHandle} into a legacy {@link TransactionHandle}
 *
 * @since 4.4.0
 */
public class LegacyTransactionHandle implements TransactionHandle {

  private final com.zernova.sdk.api.tx.TransactionHandle delegate;

  public LegacyTransactionHandle(com.zernova.sdk.api.tx.TransactionHandle delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean isTransacted() {
    return delegate.isTransacted();
  }

  @Override
  public void commit() throws TransactionException {
    delegate.commit();
  }

  @Override
  public void rollback() throws TransactionException {
    delegate.rollback();
  }

  @Override
  public void resolve() throws TransactionException {
    delegate.resolve();
  }
}
