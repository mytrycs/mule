/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.api;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.service.Service;
import com.zernova.runtime.http.api.client.HttpClient;
import com.zernova.runtime.http.api.client.HttpClientFactory;
import com.zernova.runtime.http.api.server.HttpServer;
import com.zernova.runtime.http.api.server.HttpServerFactory;
import com.zernova.runtime.http.api.utils.RequestMatcherRegistry;
import com.zernova.runtime.http.api.utils.RequestMatcherRegistry.RequestMatcherRegistryBuilder;
import com.zernova.runtime.http.api.ws.WebSocketBroadcaster;

/**
 * Provides HTTP server and client factories.
 *
 * @since 4.0
 */
@NoImplement
public interface HttpService extends Service {

  /**
   * @return an {@link HttpServerFactory} capable of creating {@link HttpServer}s.
   */
  HttpServerFactory getServerFactory();

  /**
   * @return an {@link HttpClientFactory} capable of creating {@link HttpClient}s.
   */
  HttpClientFactory getClientFactory();

  /**
   * @return a fresh builder of {@link RequestMatcherRegistry RequestMatcherRegistries}.
   * @since 4.1.5
   */
  RequestMatcherRegistryBuilder getRequestMatcherRegistryBuilder();

  /**
   * Returns a new {@link WebSocketBroadcaster}. Instances are not be assumed reusable. Unless specific implementation says
   * otherwise, create a new broadcaster per each message to be broadcasted.
   *
   * @return A new {@link WebSocketBroadcaster}
   * @since 4.2.0
   */
  default WebSocketBroadcaster newWebSocketBroadcaster() {
    throw new UnsupportedOperationException("WebSockets are only supported in Enterprise Edition");
  }

}
