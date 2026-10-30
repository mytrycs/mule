/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.api.sse.server;

import com.zernova.api.annotation.Experimental;
import com.zernova.api.annotation.NoImplement;

/**
 * Object to manage an SSE Endpoint. It allows temporarily disabling it or removing it from the server.
 * <p>
 * This API is EXPERIMENTAL. Do not use it until it is stable.
 *
 * @since 4.9.3, 4.10.0
 */
@Experimental
@NoImplement
public interface SseEndpointManager {

  /**
   * Temporarily stops the handler from being accessed, resulting in a 503 status code return by the server.
   */
  void stop();

  /**
   * Allows access to the handler.
   */
  void start();

  /**
   * Removes the handler from the server.
   */
  void dispose();
}
