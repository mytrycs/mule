/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.journal;

/**
 * Determines if the operation denotes a transaction complete log entry.
 */
public interface TransactionCompletePredicate<T> {

  /**
   * @param journalEntry journal entry
   * @return true if the journal entry completes a transaction, false otherwise.
   */
  boolean isTransactionComplete(JournalEntry<T> journalEntry);

}
