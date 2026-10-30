/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;

/**
 * Bridges a {@link SourceConnectionManager} to the {@link ConnectionProvider} contract
 *
 * @since 4.0
 */
public class SourceConnectionProvider implements ConnectionProvider<Object> {

  private final SourceConnectionManager connectionManager;
  private final ConfigurationInstance config;

  public SourceConnectionProvider(SourceConnectionManager connectionManager, ConfigurationInstance config) {
    this.connectionManager = connectionManager;
    this.config = config;
  }

  @Override
  public Object connect() throws ConnectionException {
    return connectionManager.getConnection(config);
  }

  @Override
  public void disconnect(Object connection) {
    connectionManager.release(connection);
  }

  @Override
  public ConnectionValidationResult validate(Object connection) {
    return connectionManager.testConnectivity(connection);
  }
}
