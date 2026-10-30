/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.support.internal.server;

import com.zernova.runtime.http.api.sse.server.SseResponseCustomizer;

public class SseResponseCustomizerWrapper implements com.zernova.sdk.api.http.sse.server.SseResponseCustomizer {

  private final SseResponseCustomizer customizer;

  public SseResponseCustomizerWrapper(SseResponseCustomizer customizer) {
    this.customizer = customizer;
  }

  @Override
  public com.zernova.sdk.api.http.sse.server.SseResponseCustomizer addResponseHeader(String name, String value) {
    customizer.addResponseHeader(name, value);
    return this;
  }
}
