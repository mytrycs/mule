/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.api.sse.server;

import com.zernova.api.annotation.Experimental;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

/**
 * Builder for instances of {@link SseClientConfig}.
 *
 * @since 4.10.0, 4.9.6
 */
@Experimental
public class SseClientConfigBuilder {

  private String clientId;
  private InetSocketAddress remoteAddress;
  private Consumer<SseResponseCustomizer> customizer;

  public SseClientConfig build() {
    return new SseClientConfig(clientId, remoteAddress, customizer);
  }

  public SseClientConfigBuilder withClientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  public SseClientConfigBuilder withRemoteAddress(InetSocketAddress remoteAddress) {
    this.remoteAddress = remoteAddress;
    return this;
  }

  public SseClientConfigBuilder customizeResponse(Consumer<SseResponseCustomizer> consumer) {
    this.customizer = consumer;
    return this;
  }
}
