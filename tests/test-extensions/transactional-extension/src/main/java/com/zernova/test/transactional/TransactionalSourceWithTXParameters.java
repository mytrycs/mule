/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.transactional;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;
import com.zernova.runtime.extension.api.tx.SourceTransactionalAction;
import com.zernova.test.transactional.connection.TestTransactionalConnection;

import java.util.function.Function;

public class TransactionalSourceWithTXParameters extends Source<SourceTransactionalAction, Object> {

  @Connection
  private ConnectionProvider<TestTransactionalConnection> connection;

  @Parameter
  private SourceTransactionalAction txAction;

  public static Function<Object, Object> responseCallback;

  @Override
  public void onStart(SourceCallback<SourceTransactionalAction, Object> sourceCallback) throws ZernovaException {
    responseCallback.apply(txAction);
  }

  @Override
  public void onStop() {

  }
}
