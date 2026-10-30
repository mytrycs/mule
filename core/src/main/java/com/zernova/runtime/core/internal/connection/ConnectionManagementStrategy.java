/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.connection;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionHandler;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.Closeable;

/**
 * Strategy to implement different connection management mechanisms.
 * <p>
 * For example, whether connections should be pooled, tied to an OAuth token, cached, etc.
 *
 * @param <C> the generic type of the connection being managed by {@code this} instance
 *
 * @since 1.0
 */
public abstract class ConnectionManagementStrategy<C> implements Closeable {

  protected final ConnectionProvider<C> connectionProvider;

  /**
   * Creates a new instance
   *
   * @param connectionProvider the {@link ConnectionProvider} which will be used to manage the connections
   */
  protected ConnectionManagementStrategy(ConnectionProvider<C> connectionProvider) {
    this.connectionProvider = connectionProvider;
  }

  /**
   * Wraps a connection into a {@link ConnectionHandler} and returns it. This method is to be assumed thread-safe, but no
   * assumptions should be made on whether each invokation returns the same {@link ConnectionHandler} or if that return value is
   * wrapping the same underlying {@code Connection} instance.
   *
   * @return a {@link ConnectionHandler}
   * @throws ConnectionException if an exception was found trying to obtain the connection
   */
  public abstract ConnectionHandler<C> getConnectionHandler() throws ConnectionException;

  /**
   * Closes all connections and resources allocated through {@code this} instance.
   *
   * @throws ZernovaException if an exception was found closing the connections
   */
  @Override
  public abstract void close() throws ZernovaException;
}
