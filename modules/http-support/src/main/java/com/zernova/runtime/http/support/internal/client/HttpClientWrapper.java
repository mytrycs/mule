/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.http.support.internal.client;

import com.zernova.runtime.http.api.client.HttpRequestOptions;
import com.zernova.runtime.http.support.internal.message.muletosdk.HttpResponseWrapper;
import com.zernova.runtime.http.support.internal.message.sdktomule.HttpRequestWrapper;
import com.zernova.sdk.api.http.client.HttpClient;
import com.zernova.sdk.api.http.client.HttpRequestOptionsConfigurer;
import com.zernova.sdk.api.http.domain.message.request.HttpRequest;
import com.zernova.sdk.api.http.domain.message.response.HttpResponse;
import com.zernova.sdk.api.http.sse.client.SseSource;
import com.zernova.sdk.api.http.sse.client.SseSourceConfigurer;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class HttpClientWrapper implements HttpClient {

  private final com.zernova.runtime.http.api.client.HttpClient delegate;

  public HttpClientWrapper(com.zernova.runtime.http.api.client.HttpClient delegate) {
    this.delegate = delegate;
  }

  @Override
  public CompletableFuture<HttpResponse> sendAsync(HttpRequest request,
                                                   Consumer<HttpRequestOptionsConfigurer> configurerConsumer) {
    var builder = HttpRequestOptions.builder();
    var configurer = new HttpRequestOptionsConfigToBuilder(builder);
    configurerConsumer.accept(configurer);
    var options = builder.build();

    var future = new CompletableFuture<HttpResponse>();
    delegate.sendAsync(new HttpRequestWrapper(request), options).whenComplete((res, err) -> {
      if (err != null) {
        future.completeExceptionally(err);
      } else {
        future.complete(new HttpResponseWrapper(res));
      }
    });
    return future;
  }

  @Override
  public SseSource sseSource(Consumer<SseSourceConfigurer> configConsumer) {
    SseSourceConfigImpl configurer = new SseSourceConfigImpl();
    configConsumer.accept(configurer);
    return new SseSourceWrapper(delegate.sseSource(configurer.build()));
  }

  @Override
  public void start() {
    delegate.start();
  }

  @Override
  public void stop() {
    delegate.stop();
  }
}
