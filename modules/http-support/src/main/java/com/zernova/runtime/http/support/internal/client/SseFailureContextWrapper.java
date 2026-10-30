/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.support.internal.client;

import com.zernova.runtime.http.api.sse.client.SseFailureContext;
import com.zernova.runtime.http.support.internal.message.muletosdk.HttpResponseWrapper;
import com.zernova.sdk.api.http.domain.message.response.HttpResponse;

public class SseFailureContextWrapper implements com.zernova.sdk.api.http.sse.client.SseFailureContext {

  private final SseFailureContext ctx;

  public SseFailureContextWrapper(SseFailureContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Throwable error() {
    return ctx.error();
  }

  @Override
  public HttpResponse response() {
    return new HttpResponseWrapper(ctx.response());
  }

  @Override
  public void stopRetrying() {
    ctx.stopRetrying();
  }
}
