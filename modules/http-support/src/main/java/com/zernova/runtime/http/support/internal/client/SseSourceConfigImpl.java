/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.http.support.internal.client;

import static com.zernova.runtime.http.api.sse.client.SseSourceConfig.fromResponse;

import com.zernova.runtime.http.api.client.HttpRequestOptions;
import com.zernova.runtime.http.api.sse.client.SseRetryConfig;
import com.zernova.runtime.http.support.internal.message.HttpRequestBuilderWrapper;
import com.zernova.runtime.http.support.internal.message.sdktomule.HttpResponseWrapper;
import com.zernova.sdk.api.http.client.HttpRequestOptionsConfigurer;
import com.zernova.sdk.api.http.domain.message.request.HttpRequestBuilder;
import com.zernova.sdk.api.http.domain.message.response.HttpResponse;
import com.zernova.sdk.api.http.sse.client.SseSourceConfigurer;

import java.util.function.Consumer;

public class SseSourceConfigImpl implements SseSourceConfigurer {

  private HttpResponse response;

  private String url;
  private boolean allowRetryDelayOverride;
  private long initialRetryDelayMillis;
  private boolean shouldRetryOnStreamEnd;
  private Consumer<HttpRequestBuilder> requestCustomizer;
  private Consumer<HttpRequestOptionsConfigurer> requestOptionsConfigConsumer;
  private boolean preserveHeadersCase;

  @Override
  public SseSourceConfigurer withResponse(HttpResponse response) {
    this.response = response;
    return this;
  }

  @Override
  public SseSourceConfigurer withUrl(String url) {
    this.url = url;
    return this;
  }

  @Override
  public SseSourceConfigurer withAllowRetryDelayOverride(boolean allow) {
    this.allowRetryDelayOverride = allow;
    return this;
  }

  @Override
  public SseSourceConfigurer withInitialRetryDelayMillis(long initialRetryDelayMillis) {
    this.initialRetryDelayMillis = initialRetryDelayMillis;
    return this;
  }

  @Override
  public SseSourceConfigurer withRetry(boolean shouldRetryOnStreamEnd) {
    this.shouldRetryOnStreamEnd = shouldRetryOnStreamEnd;
    return this;
  }

  @Override
  public SseSourceConfigurer withRequestCustomizer(Consumer<HttpRequestBuilder> requestCustomizer) {
    this.requestCustomizer = requestCustomizer;
    return this;
  }

  @Override
  public SseSourceConfigurer withRequestOptions(Consumer<HttpRequestOptionsConfigurer> requestOptions) {
    this.requestOptionsConfigConsumer = requestOptions;
    return this;
  }

  @Override
  public SseSourceConfigurer withPreserveHeadersCase(boolean preserveHeadersCase) {
    this.preserveHeadersCase = preserveHeadersCase;
    return this;
  }

  public com.zernova.runtime.http.api.sse.client.SseSourceConfig build() {
    if (response == null && url == null) {
      throw new IllegalArgumentException("An HTTP Response or an URL must be provided to build an SseSource");
    }

    if (response != null) {
      return fromResponse(new HttpResponseWrapper(response)).build();
    } else {
      var optionsBuilder = HttpRequestOptions.builder();
      if (requestOptionsConfigConsumer != null) {
        var optionsConfigurer = new HttpRequestOptionsConfigToBuilder(optionsBuilder);
        requestOptionsConfigConsumer.accept(optionsConfigurer);
      }

      return com.zernova.runtime.http.api.sse.client.SseSourceConfig
          .builder(url)
          .withRetryConfig(new SseRetryConfig(allowRetryDelayOverride, initialRetryDelayMillis, shouldRetryOnStreamEnd))
          .withRequestCustomizer(b -> requestCustomizer.accept(new HttpRequestBuilderWrapper(b)))
          .withRequestOptions(optionsBuilder.build())
          .withPreserveHeadersCase(preserveHeadersCase)
          .build();
    }
  }
}
