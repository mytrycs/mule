/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.transactionalxa;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.sdk.api.annotation.param.Connection;
import com.zernova.sdk.api.annotation.param.Parameter;
import com.zernova.sdk.api.connectivity.ConnectionProvider;
import com.zernova.sdk.api.runtime.source.Source;
import com.zernova.sdk.api.runtime.source.SourceCallback;
import com.zernova.sdk.api.tx.SourceTransactionalAction;
import com.zernova.test.transactionalxa.connection.TestTransactionalConnection;

import java.util.function.Function;

public class TransactionalSourceWithTXParameters extends Source<SourceTransactionalAction, Object> {

  @Connection
  private ConnectionProvider<TestTransactionalConnection> connection;

  @Parameter
  private SourceTransactionalAction transactionalAction;

  public static Function<Object, Object> responseCallback;

  @Override
  public void onStart(SourceCallback<SourceTransactionalAction, Object> sourceCallback) throws ZernovaException {
    responseCallback.apply(transactionalAction);
  }

  @Override
  public void onStop() {

  }
}
