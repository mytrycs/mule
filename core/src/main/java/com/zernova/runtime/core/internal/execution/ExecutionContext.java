/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.execution;

import com.zernova.runtime.core.api.transaction.TransactionCoordination;

/**
 * Provides information about the current execution of an {@link com.zernova.runtime.core.api.execution.ExecutionTemplate}
 */
public class ExecutionContext {

  private boolean transactionStarted;

  /**
   * @return true if the current transaction must be resolved within the current context
   */
  public boolean needsTransactionResolution() {
    return transactionStarted && TransactionCoordination.getInstance().getTransaction() != null;
  }

  /**
   * This method must be called whenever a transaction has been created in the execution context
   */
  public void markTransactionStart() {
    this.transactionStarted = true;
  }

}
