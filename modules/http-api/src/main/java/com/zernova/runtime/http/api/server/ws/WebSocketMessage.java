/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.api.server.ws;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.http.api.ws.WebSocket;

import java.io.InputStream;

/**
 * A message received into an inbound {@link WebSocket}
 *
 * @since 4.2.0
 */
@NoImplement
public interface WebSocketMessage {

  /**
   * @return The {@link WebSocket} that received the message
   */
  WebSocket getSocket();

  /**
   * @return The request that initiated the {@link WebSocket} connection
   */
  WebSocketRequest getRequest();

  /**
   * @return The message content
   */
  TypedValue<InputStream> getContent();
}
