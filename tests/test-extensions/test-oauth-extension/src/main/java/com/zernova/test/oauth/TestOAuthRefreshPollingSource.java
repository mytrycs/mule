/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.oauth;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.connectivity.oauth.AccessTokenExpiredException;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.PollContext;
import com.zernova.runtime.extension.api.runtime.source.PollingSource;
import com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext;

@MediaType(TEXT_PLAIN)
@Alias("poller")
public class TestOAuthRefreshPollingSource extends PollingSource<String, String> {

  public static final String SOURCE_ITEM_PAYLOAD = "Item payload!";
  public static final String SOURCE_ITEM_ATTRIBUTE = "Item attribute!";

  @Connection
  private ConnectionProvider<TestOAuthConnection> connectionProvider;

  @Override
  protected void doStart() throws ZernovaException {
    // Do nothing
  }

  @Override
  protected void doStop() {
    // Do nothing
  }

  @Override
  public void poll(PollContext<String, String> pollContext) {
    TestOAuthConnection testOAuthConnection = null;
    try {
      testOAuthConnection = connectionProvider.connect();
    } catch (ConnectionException e) {
      throw new ZernovaRuntimeException(e);
    }
    if (!testOAuthConnection.getState().getState().getAccessToken().contains("refreshed")) {
      pollContext.onConnectionException(new ConnectionException(new AccessTokenExpiredException("Token is expired!")));
      return;
    }
    pollContext.accept(pollItem -> pollItem
        .setResult(Result.<String, String>builder().output(SOURCE_ITEM_PAYLOAD).attributes(SOURCE_ITEM_ATTRIBUTE).build()));
    connectionProvider.disconnect(testOAuthConnection);
  }

  @Override
  public void onRejectedItem(Result<String, String> result, SourceCallbackContext callbackContext) {
    // Do nothing
  }

}
