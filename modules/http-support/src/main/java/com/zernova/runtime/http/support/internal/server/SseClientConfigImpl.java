/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.http.support.internal.server;

import com.zernova.sdk.api.http.domain.message.request.HttpRequestContext;
import com.zernova.sdk.api.http.sse.server.SseClientConfigurer;
import com.zernova.sdk.api.http.sse.server.SseResponseCustomizer;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

public class SseClientConfigImpl implements SseClientConfigurer {

  private String clientId;
  private InetSocketAddress remoteAddress;
  private Consumer<SseResponseCustomizer> responseCustomizer;

  @Override
  public SseClientConfigurer withRequestContext(HttpRequestContext requestContext) {
    return withRemoteAddress(nullSafeGetAddress(requestContext));
  }

  @Override
  public SseClientConfigurer withClientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  @Override
  public SseClientConfigurer withRemoteAddress(InetSocketAddress remoteAddress) {
    this.remoteAddress = remoteAddress;
    return this;
  }

  @Override
  public SseClientConfigurer customizeResponse(Consumer<SseResponseCustomizer> callback) {
    this.responseCustomizer = callback;
    return this;
  }

  public com.zernova.runtime.http.api.sse.server.SseClientConfig build() {
    return com.zernova.runtime.http.api.sse.server.SseClientConfig.builder()
        .withClientId(clientId)
        .withRemoteAddress(remoteAddress)
        .customizeResponse(customizer -> responseCustomizer.accept(new SseResponseCustomizerWrapper(customizer)))
        .build();
  }

  private static InetSocketAddress nullSafeGetAddress(HttpRequestContext requestContext) {
    if (requestContext == null) {
      return null;
    }
    var clientConnection = requestContext.getClientConnection();
    if (clientConnection == null) {
      return null;
    }
    return clientConnection.getRemoteHostAddress();
  }
}
