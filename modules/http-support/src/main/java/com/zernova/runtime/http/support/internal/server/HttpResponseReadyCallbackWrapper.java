/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.http.support.internal.server;

import com.zernova.runtime.http.support.internal.message.sdktomule.HttpResponseWrapper;
import com.zernova.sdk.api.http.domain.message.response.HttpResponse;
import com.zernova.sdk.api.http.server.async.HttpResponseReadyCallback;
import com.zernova.sdk.api.http.server.async.ResponseStatusCallback;
import com.zernova.sdk.api.http.sse.server.SseClient;
import com.zernova.sdk.api.http.sse.server.SseClientConfigurer;

import java.util.function.Consumer;

public class HttpResponseReadyCallbackWrapper implements HttpResponseReadyCallback {

  private final com.zernova.runtime.http.api.server.async.HttpResponseReadyCallback muleResponseCallback;

  public HttpResponseReadyCallbackWrapper(com.zernova.runtime.http.api.server.async.HttpResponseReadyCallback muleResponseCallback) {
    this.muleResponseCallback = muleResponseCallback;
  }

  @Override
  public void responseReady(HttpResponse response, ResponseStatusCallback responseStatusCallback) {
    muleResponseCallback.responseReady(new HttpResponseWrapper(response),
                                       new ResponseStatusCallbackWrapper(responseStatusCallback));
  }

  @Override
  public SseClient startSseResponse(Consumer<SseClientConfigurer> configConsumer) {
    var configurer = new SseClientConfigImpl();
    configConsumer.accept(configurer);
    return new SseClientWrapper(muleResponseCallback.startSseResponse(configurer.build()));
  }
}
