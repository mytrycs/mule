/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.implicit.config.extension.extension.internal;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.test.implicit.config.extension.extension.api.Counter;

public class ImplicitConnectionProvider implements ConnectionProvider<Counter> {

  @ParameterGroup(name = "Internal")
  private PrivateParameterGroup privateGroup;

  @Parameter
  @Optional(defaultValue = "#[vars.number]")
  private int number;

  @Override
  public Counter connect() throws ConnectionException {
    if (privateGroup == null) {
      throw new ConnectionException("Internal parameter group was not instantiate with nullsafe");
    }
    return new Counter(number);
  }

  @Override
  public void disconnect(Counter counter) {

  }

  @Override
  public ConnectionValidationResult validate(Counter counter) {
    return ConnectionValidationResult.success();
  }
}
