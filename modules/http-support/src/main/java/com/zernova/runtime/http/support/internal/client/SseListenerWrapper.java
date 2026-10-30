/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.support.internal.client;

import com.zernova.runtime.http.api.sse.ServerSentEvent;
import com.zernova.runtime.http.api.sse.client.SseListener;
import com.zernova.runtime.http.support.internal.message.muletosdk.ServerSentEventWrapper;

public class SseListenerWrapper implements SseListener {

  private final com.zernova.sdk.api.http.sse.client.SseListener listener;

  public SseListenerWrapper(com.zernova.sdk.api.http.sse.client.SseListener listener) {
    this.listener = listener;
  }

  @Override
  public void onEvent(ServerSentEvent event) {
    listener.onEvent(new ServerSentEventWrapper(event));
  }

  @Override
  public void onClose() {
    listener.onClose();
  }
}
