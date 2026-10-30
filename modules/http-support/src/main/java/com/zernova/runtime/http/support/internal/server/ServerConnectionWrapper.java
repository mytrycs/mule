/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.support.internal.server;

import com.zernova.sdk.api.http.domain.message.request.ServerConnection;

import java.net.InetSocketAddress;

public class ServerConnectionWrapper implements ServerConnection {

  private final com.zernova.runtime.http.api.domain.request.ServerConnection muleServerConnection;

  public ServerConnectionWrapper(com.zernova.runtime.http.api.domain.request.ServerConnection muleServerConnection) {
    this.muleServerConnection = muleServerConnection;
  }

  @Override
  public InetSocketAddress getLocalHostAddress() {
    return muleServerConnection.getLocalHostAddress();
  }
}
