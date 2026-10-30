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
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.connectivity.NoConnectivityTest;
import com.zernova.tck.testmodels.fruit.Apple;

@Alias("apple")
public class VeganAppleConnectionProvider implements ConnectionProvider<Apple>, NoConnectivityTest {

  @Override
  public Apple connect() throws ConnectionException {
    return new Apple();
  }

  @Override
  public void disconnect(Apple apple) {

  }

  @Override
  public ConnectionValidationResult validate(Apple apple) {
    return ConnectionValidationResult.success();
  }
}
