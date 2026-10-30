/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.transaction;

import com.zernova.runtime.api.connection.ConnectionHandler;
import com.zernova.runtime.core.api.transaction.Transaction;
import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.extension.api.tx.Transactional;
import com.zernova.sdk.api.connectivity.TransactionalConnection;

/**
 * Generic transactional resource for extension's {@link TransactionalConnection}s
 *
 * @param <T> the generic type of the {@link TransactionalConnection}
 * @since 4.0
 */
public class ExtensionTransactionalResource<T extends TransactionalConnection> implements Transactional {

  private final T connection;
  private final ConnectionHandler<T> connectionHandler;
  private final Transaction transaction;

  /**
   * Creates a new instance
   *
   * @param connection        the connection
   * @param connectionHandler the {@link ConnectionHandler} for the {@code connection}
   * @param transaction       the bound {@link Transaction}
   */
  public ExtensionTransactionalResource(T connection, ConnectionHandler<T> connectionHandler, Transaction transaction) {
    this.connection = connection;
    this.connectionHandler = connectionHandler;
    this.transaction = transaction;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void begin() throws TransactionException {
    connection.begin();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void commit() throws TransactionException {
    try {
      connection.commit();
    } finally {
      connectionHandler.release();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void rollback() throws TransactionException {
    try {
      connection.rollback();
    } finally {
      connectionHandler.release();
    }
  }

  /**
   * @return whether the transaction is commited or rolled back
   * @throws TransactionException
   */
  public boolean isTransactionResolved() throws TransactionException {
    return transaction.isCommitted() || transaction.isRolledBack();
  }

  /**
   * @return the {@link TransactionalConnection}
   */
  public T getConnection() {
    return connection;
  }

  /**
   * @return the {@link ConnectionHandler}
   */
  public ConnectionHandler<T> getConnectionHandler() {
    return connectionHandler;
  }
}
