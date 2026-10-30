/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.queue;

import com.zernova.runtime.core.api.transaction.xa.ResourceManagerException;

/**
 * Specialization of {@link QueueTransactionContext} for local queue transactions
 */
public interface LocalQueueTransactionContext extends QueueTransactionContext {

  /**
   * Commits the current transaction context operations
   *
   * @throws ResourceManagerException
   */
  void doCommit() throws ResourceManagerException;

  /**
   * Rollbacks the current transaction context operations
   *
   * @throws ResourceManagerException
   */
  void doRollback() throws ResourceManagerException;

}
