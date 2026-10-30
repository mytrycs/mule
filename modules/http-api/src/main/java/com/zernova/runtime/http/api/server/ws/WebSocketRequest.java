/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.api.server.ws;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.metadata.MediaType;
import com.zernova.runtime.api.util.MultiMap;
import com.zernova.runtime.http.api.domain.message.MessageWithHeaders;
import com.zernova.runtime.http.api.domain.request.ClientConnection;
import com.zernova.runtime.http.api.domain.request.ServerConnection;
import com.zernova.runtime.http.api.ws.WebSocket;
import com.zernova.runtime.http.api.ws.WebSocketProtocol;

import java.net.URI;

/**
 * Represents an HTTP request with an upgrade header to initiate a {@link WebSocket}
 *
 * @since 4.2.0
 */
@NoImplement
public interface WebSocketRequest extends MessageWithHeaders {

  /**
   * @return The request path
   */
  String getPath();

  /**
   * @return The request {@link MediaType}
   */
  MediaType getContentType();

  /**
   * @return The query params
   */
  MultiMap<String, String> getQueryParams();

  /**
   * @return The scheme
   */
  WebSocketProtocol getScheme();

  /**
   * @return The protocol version of the HTTP request
   */
  String getHttpVersion();

  /**
   * @return The HTTP request method
   */
  String getMethod();

  /**
   * @return The request {@link URI}
   */
  URI getRequestUri();

  /**
   * @return the server connection descriptor
   */
  ServerConnection getServerConnection();

  /**
   * @return the client connection descriptor
   */
  ClientConnection getClientConnection();
}
