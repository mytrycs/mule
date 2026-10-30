/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.support.internal.server;

import com.zernova.runtime.http.support.internal.message.muletosdk.HttpRequestWrapper;
import com.zernova.sdk.api.http.domain.message.request.ClientConnection;
import com.zernova.sdk.api.http.domain.message.request.HttpRequest;
import com.zernova.sdk.api.http.domain.message.request.HttpRequestContext;
import com.zernova.sdk.api.http.domain.message.request.ServerConnection;

public class HttpRequestContextWrapper implements HttpRequestContext {

  private final com.zernova.runtime.http.api.domain.request.HttpRequestContext muleRequestContext;

  public HttpRequestContextWrapper(com.zernova.runtime.http.api.domain.request.HttpRequestContext requestContext) {
    this.muleRequestContext = requestContext;
  }

  @Override
  public String getScheme() {
    return muleRequestContext.getScheme();
  }

  @Override
  public HttpRequest getRequest() {
    return new HttpRequestWrapper(muleRequestContext.getRequest());
  }

  @Override
  public ServerConnection getServerConnection() {
    return new ServerConnectionWrapper(muleRequestContext.getServerConnection());
  }

  @Override
  public ClientConnection getClientConnection() {
    return new ClientConnectionWrapper(muleRequestContext.getClientConnection());
  }
}
