/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.transaction;

import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.extension.api.tx.TransactionHandle;

/**
 * Null implementation of {@link TransactionHandle}. Use in cases in which no transaciton is actually active
 */
public class NullTransactionHandle implements TransactionHandle {


  /**
   * {@inheritDoc}
   *
   * @return {@code false}
   */
  @Override
  public boolean isTransacted() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void commit() throws TransactionException {
    // no - op
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void rollback() throws TransactionException {
    // no - op
  }

  @Override
  public void resolve() throws TransactionException {
    // no - op
  }
}
