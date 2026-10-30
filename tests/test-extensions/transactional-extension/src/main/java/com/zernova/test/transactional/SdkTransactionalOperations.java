/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.transactional;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.api.util.Preconditions.checkState;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.extension.api.annotation.metadata.OutputResolver;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Content;
import com.zernova.test.transactional.connection.SdkTestTransactionalConnection;

import java.util.concurrent.atomic.AtomicInteger;

public class SdkTransactionalOperations {

  private AtomicInteger connectionExceptions = new AtomicInteger(0);
  public static Integer getPageCalls = 0;

  @OutputResolver(output = TransactionalMetadataResolver.class)
  public SdkTestTransactionalConnection sdkGetConnection(@Connection SdkTestTransactionalConnection sdkConnection) {
    return sdkConnection;
  }

  public void sdkVerifyNoTransaction(@Connection SdkTestTransactionalConnection sdkConnection) {
    checkState(!sdkConnection.isTransactionBegun(), "transaction begun with no reason");
  }

  public void sdkVerifyTransactionBegun(@com.zernova.sdk.api.annotation.param.Connection SdkTestTransactionalConnection sdkConnection) {
    checkState(sdkConnection.isTransactionBegun(), "transaction not begun");
  }

  public void sdkVerifySameConnection(@Connection SdkTestTransactionalConnection sdkConnection,
                                      @Content SdkTestTransactionalConnection sdkTransactionalConnection) {
    checkArgument(sdkTransactionalConnection != null, "The transactionalConnection can't be null");
    checkState(sdkConnection.getConnectionId() == sdkTransactionalConnection.getConnectionId(), "The connection is not the same");
  }

  public void sdkFail() {
    throw new RuntimeException("you better rollback!");
  }

  public void sdkConnectionException(@Connection SdkTestTransactionalConnection sdkConnection) throws ConnectionException {
    throw new ConnectionException(String.valueOf(connectionExceptions.incrementAndGet()));
  }
}
