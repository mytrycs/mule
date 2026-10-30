/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension.connection;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.test.values.extension.ValuesConnection;

public class ValuesConnectionProvider implements ConnectionProvider<ValuesConnection> {

  @Parameter
  @Optional
  String someParameter;

  @Override
  public ValuesConnection connect() throws ConnectionException {
    return new ValuesConnection();
  }

  @Override
  public void disconnect(ValuesConnection optionsConnection) {

  }

  @Override
  public ConnectionValidationResult validate(ValuesConnection optionsConnection) {
    return ConnectionValidationResult.success();
  }
}
