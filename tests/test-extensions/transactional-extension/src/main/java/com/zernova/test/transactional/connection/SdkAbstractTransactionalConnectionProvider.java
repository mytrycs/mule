/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.transactional.connection;

import static com.zernova.runtime.api.connection.ConnectionValidationResult.success;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

abstract class SdkAbstractTransactionalConnectionProvider implements ConnectionProvider<SdkTestTransactionalConnection> {

  private DummyXaResource dummyXaResource = new DummyXaResource();

  @Parameter
  @Optional(defaultValue = "false")
  boolean useXa;

  @Override
  public SdkTestTransactionalConnection connect() throws ConnectionException {
    return useXa ? new SdkTestXaTransactionalConnection(dummyXaResource) : new SdkTestLocalTransactionalConnection();
  }

  @Override
  public void disconnect(SdkTestTransactionalConnection testTransactionalConnection) {
    testTransactionalConnection.disconnect();
  }

  @Override
  public ConnectionValidationResult validate(SdkTestTransactionalConnection testTransactionalConnection) {
    return success();
  }

}
