/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.extension.test.extension.reconnection;

import static com.zernova.runtime.api.connection.ConnectionValidationResult.failure;

import com.zernova.runtime.api.connection.CachedConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.Alias;

@Alias("failing-connection")
public class FailingConnectionConnectionProvider implements CachedConnectionProvider<FailingConnection> {

  @Override
  public FailingConnection connect() throws ConnectionException {
    return new FailingConnection();
  }

  @Override
  public void disconnect(FailingConnection connection) {
    // do nothing
  }

  @Override
  public ConnectionValidationResult validate(FailingConnection connection) {
    return failure("Connection failure", new ConnectionException("Connection failure"));
  }
}
