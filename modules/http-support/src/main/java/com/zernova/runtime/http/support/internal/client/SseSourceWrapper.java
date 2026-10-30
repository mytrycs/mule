/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.support.internal.client;

import com.zernova.sdk.api.http.sse.client.SseFailureContext;
import com.zernova.sdk.api.http.sse.client.SseListener;
import com.zernova.sdk.api.http.sse.client.SseSource;

import java.util.function.Consumer;

public class SseSourceWrapper implements SseSource {

  private final com.zernova.runtime.http.api.sse.client.SseSource sseSource;

  public SseSourceWrapper(com.zernova.runtime.http.api.sse.client.SseSource sseSource) {
    this.sseSource = sseSource;
  }

  @Override
  public void open() {
    sseSource.open();
  }

  @Override
  public int getReadyState() {
    return sseSource.getReadyState();
  }

  @Override
  public void register(String eventName, SseListener listener) {
    sseSource.register(eventName, new SseListenerWrapper(listener));
  }

  @Override
  public void register(SseListener listener) {
    sseSource.register(new SseListenerWrapper(listener));
  }

  @Override
  public void doOnConnectionFailure(Consumer<SseFailureContext> onConnectionFailure) {
    sseSource.doOnConnectionFailure(ctx -> onConnectionFailure.accept(new SseFailureContextWrapper(ctx)));
  }

  @Override
  public void close() {
    sseSource.close();
  }
}
