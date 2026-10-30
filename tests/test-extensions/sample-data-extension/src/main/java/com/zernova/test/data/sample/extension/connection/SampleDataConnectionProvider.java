/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.data.sample.extension.connection;


import static com.zernova.runtime.api.connection.ConnectionValidationResult.success;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.test.data.sample.extension.SampleDataConnection;

public class SampleDataConnectionProvider implements ConnectionProvider<SampleDataConnection> {

  @Override
  public SampleDataConnection connect() throws ConnectionException {
    return new SampleDataConnection();
  }

  @Override
  public void disconnect(SampleDataConnection connection) {

  }

  @Override
  public ConnectionValidationResult validate(SampleDataConnection connection) {
    return success();
  }
}
