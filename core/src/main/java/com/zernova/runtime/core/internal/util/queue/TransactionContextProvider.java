/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.queue;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;

/**
 * Provides access to the transaction context if there is one for the current session.
 */
public interface TransactionContextProvider {

  /**
   * @return true if there is a transaction context created for the current session
   */
  boolean isTransactional();

  /**
   * @return if {@link #isTransactional()} returns true it will return the {@link QueueTransactionContext} related to the current
   *         session.
   * @throws {@link ZernovaRuntimeException} if {@link #isTransactional()} is false.
   */
  QueueTransactionContext getTransactionalContext();
}
