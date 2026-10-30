/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;

import java.util.concurrent.ExecutorService;

@Alias("source")
@MediaType(TEXT_PLAIN)
public class FailingPetStoreSource extends Source<String, Object> {

  @Config
  PetStoreConnector config;

  @Connection
  private ConnectionProvider<PetStoreClient> connectionProvider;

  @Parameter
  @Optional(defaultValue = "false")
  boolean failOnStart;

  @Parameter
  @Optional(defaultValue = "false")
  boolean failOnException;

  public static boolean failedDueOnException = false;
  public static ConnectionException connectionException = new ConnectionException("ERROR");
  public static ExecutorService executor;

  @Override
  public void onStart(SourceCallback<String, Object> sourceCallback) throws ZernovaException {
    final int t = PetStoreConnector.incTimesStarted();

    if (failOnStart || failedDueOnException) {
      throw new RuntimeException(connectionException);
    }

    if (failOnException) {
      failedDueOnException = true;
      executor = newSingleThreadExecutor();
      executor.execute(() -> sourceCallback.onConnectionException(connectionException));
    }
  }

  @Override
  public void onStop() {}
}
