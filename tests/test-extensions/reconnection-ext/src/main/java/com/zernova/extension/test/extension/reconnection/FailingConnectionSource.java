/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.extension.test.extension.reconnection;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Alias("failing-connection-source")
public class FailingConnectionSource extends Source<List<FailingConnection>, Void> {

  public static Integer failures = 0;
  private static List<FailingConnection> connections = Collections.synchronizedList(new ArrayList<>());

  @Connection
  ConnectionProvider<FailingConnection> connectionProvider;

  FailingConnection connection;

  @Override
  public void onStart(SourceCallback<List<FailingConnection>, Void> sourceCallback) throws ZernovaException {
    connection = connectionProvider.connect();
    try {
      connections.add(connection);
      if (failures < 4) {
        failures += 1;
        connection.sendWithFailure("failure");
      } else {
        connection.send("successful");
        sourceCallback.handle(Result.<List<FailingConnection>, Void>builder().output(connections).build());
      }
    } catch (Exception e) {
      throw new ConnectionException(e, connection);
    }
  }

  @Override
  public void onStop() {
    connectionProvider.disconnect(connection);
  }
}
