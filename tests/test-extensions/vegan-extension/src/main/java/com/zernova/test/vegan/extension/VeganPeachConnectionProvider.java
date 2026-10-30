/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.tck.testmodels.fruit.Peach;

public class VeganPeachConnectionProvider implements ConnectionProvider<Peach> {

  @Override
  public Peach connect() throws ConnectionException {
    return new Peach();
  }

  @Override
  public void disconnect(Peach peach) {

  }

  @Override
  public ConnectionValidationResult validate(Peach peach) {
    return ConnectionValidationResult.success();
  }
}
