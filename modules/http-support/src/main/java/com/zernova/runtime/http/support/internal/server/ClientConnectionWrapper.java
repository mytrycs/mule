/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.support.internal.server;

import com.zernova.sdk.api.http.domain.message.request.ClientConnection;

import java.net.InetSocketAddress;
import java.security.cert.Certificate;

public class ClientConnectionWrapper implements ClientConnection {

  private final com.zernova.runtime.http.api.domain.request.ClientConnection muleClientConnection;

  public ClientConnectionWrapper(com.zernova.runtime.http.api.domain.request.ClientConnection muleClientConnection) {
    this.muleClientConnection = muleClientConnection;
  }

  @Override
  public InetSocketAddress getRemoteHostAddress() {
    return muleClientConnection.getRemoteHostAddress();
  }

  @Override
  public Certificate getClientCertificate() {
    return muleClientConnection.getClientCertificate();
  }
}
