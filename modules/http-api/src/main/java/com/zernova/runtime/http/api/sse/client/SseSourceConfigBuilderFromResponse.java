/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.api.sse.client;

import static com.zernova.runtime.http.api.server.HttpServerProperties.PRESERVE_HEADER_CASE;
import static com.zernova.runtime.http.api.sse.client.SseRetryConfig.noRetry;

import com.zernova.api.annotation.Experimental;
import com.zernova.runtime.http.api.domain.message.response.HttpResponse;

/**
 * Builder for instances of {@link SseSourceConfig} to be used when you already have an SSE response. If you want to configure a
 * {@link SseSource} to send the initiator request and do retries, use {@link SseSourceConfig#builder(String)} instead.
 * <p>
 * This API is EXPERIMENTAL. Do not use it until it is stable.
 *
 * @since 4.10.0, 4.9.6
 */
@Experimental
public class SseSourceConfigBuilderFromResponse {

  private final HttpResponse response;

  public SseSourceConfigBuilderFromResponse(HttpResponse response) {
    this.response = response;
  }

  public SseSourceConfig build() {
    return new SseSourceConfig(null, noRetry(), b -> {
    }, null, PRESERVE_HEADER_CASE, response);
  }
}
