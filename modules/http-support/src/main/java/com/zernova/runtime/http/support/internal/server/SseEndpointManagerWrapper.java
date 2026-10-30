/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.support.internal.server;

import com.zernova.sdk.api.http.sse.server.SseEndpointManager;

public class SseEndpointManagerWrapper implements SseEndpointManager {

  private final com.zernova.runtime.http.api.sse.server.SseEndpointManager delegate;

  public SseEndpointManagerWrapper(com.zernova.runtime.http.api.sse.server.SseEndpointManager delegate) {
    this.delegate = delegate;
  }

  @Override
  public void stop() {
    delegate.stop();
  }

  @Override
  public void start() {
    delegate.start();
  }

  @Override
  public void dispose() {
    delegate.dispose();
  }
}
