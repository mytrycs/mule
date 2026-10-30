/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.transactional.connection;

import com.zernova.runtime.api.tx.TransactionException;

public class TestLocalTransactionalConnection implements TestTransactionalConnection {

  private boolean transactionBegun, transactionCommited, transactionRolledback = false;
  private boolean connected = true;
  private double connectionId;

  public TestLocalTransactionalConnection() {
    connectionId = Math.random();
  }

  @Override
  public void begin() throws TransactionException {
    transactionBegun = true;
  }

  @Override
  public void commit() throws TransactionException {
    transactionCommited = true;
  }

  @Override
  public void rollback() throws TransactionException {
    transactionRolledback = true;
  }

  @Override
  public double getConnectionId() {
    return connectionId;
  }

  @Override
  public boolean isTransactionBegun() {
    return transactionBegun;
  }

  @Override
  public boolean isTransactionCommited() {
    return transactionCommited;
  }

  @Override
  public boolean isTransactionRolledback() {
    return transactionRolledback;
  }

  @Override
  public void disconnect() {
    connected = false;
  }

  @Override
  public boolean isConnected() {
    return connected;
  }
}
