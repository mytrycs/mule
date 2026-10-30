/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transaction;

import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.core.api.transaction.Transaction;

/**
 * Adapter interface to access {@link Transaction} suspension functionality that we don't want exposed as part of the public API
 *
 * @since 4.9
 */
public interface SuspendableTransaction extends Transaction {

  /**
   * Resume the XA transaction
   *
   * @throws TransactionException if any error
   */
  void resume() throws TransactionException;

  /**
   * Suspend the XA transaction
   *
   * @throws TransactionException if any error
   */
  jakarta.transaction.Transaction suspend() throws TransactionException;
}
