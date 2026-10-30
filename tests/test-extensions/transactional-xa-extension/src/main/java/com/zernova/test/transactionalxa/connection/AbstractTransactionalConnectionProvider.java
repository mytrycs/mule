/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.transactionalxa.connection;

import static com.zernova.sdk.api.connectivity.ConnectionValidationResult.success;

import com.zernova.runtime.api.config.PoolingProfile;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.sdk.api.annotation.param.Optional;
import com.zernova.sdk.api.annotation.param.Parameter;
import com.zernova.sdk.api.connectivity.ConnectionValidationResult;
import com.zernova.sdk.api.connectivity.XATransactionalConnectionProvider;

abstract class AbstractTransactionalConnectionProvider
    implements XATransactionalConnectionProvider<TestTransactionalConnection> {

  private DummyXaResource dummyXaResource = new DummyXaResource();

  @Parameter
  @Optional(defaultValue = "false")
  boolean useXa;

  @Override
  public TestTransactionalConnection connect() throws ConnectionException {
    return useXa ? new TestXaTransactionalConnection(dummyXaResource) : new TestLocalTransactionalConnection();
  }

  @Override
  public void disconnect(TestTransactionalConnection testTransactionalConnection) {
    testTransactionalConnection.disconnect();
  }

  @Override
  public ConnectionValidationResult validate(TestTransactionalConnection testTransactionalConnection) {
    return success();
  }

  @Override
  public PoolingProfile getXaPoolingProfile() {
    return new PoolingProfile();
  }

}
