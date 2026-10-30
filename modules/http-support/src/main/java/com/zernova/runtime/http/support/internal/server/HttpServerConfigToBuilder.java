/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.http.support.internal.server;

import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.api.tls.TlsContextFactory;
import com.zernova.runtime.http.api.server.HttpServerConfiguration;
import com.zernova.sdk.api.http.server.HttpServerConfigurer;

import java.util.function.Supplier;

public final class HttpServerConfigToBuilder implements HttpServerConfigurer {

  private final HttpServerConfiguration.Builder builder;

  public HttpServerConfigToBuilder(HttpServerConfiguration.Builder builder) {
    this.builder = builder;
  }

  @Override
  public HttpServerConfigurer setHost(String host) {
    builder.setHost(host);
    return this;
  }

  @Override
  public HttpServerConfigurer setPort(int port) {
    builder.setPort(port);
    return this;
  }

  @Override
  public HttpServerConfigurer setTlsContextFactory(TlsContextFactory tlsContextFactory) {
    builder.setTlsContextFactory(tlsContextFactory);
    return this;
  }

  @Override
  public HttpServerConfigurer setUsePersistentConnections(boolean usePersistentConnections) {
    builder.setUsePersistentConnections(usePersistentConnections);
    return this;
  }

  @Override
  public HttpServerConfigurer setConnectionIdleTimeout(int connectionIdleTimeout) {
    builder.setConnectionIdleTimeout(connectionIdleTimeout);
    return this;
  }

  @Override
  public HttpServerConfigurer setSchedulerSupplier(Supplier<Scheduler> schedulerSupplier) {
    builder.setSchedulerSupplier(schedulerSupplier);
    return this;
  }

  @Override
  public HttpServerConfigurer setName(String name) {
    builder.setName(name);
    return this;
  }

  @Override
  public HttpServerConfigurer setReadTimeout(long readTimeout) {
    builder.setReadTimeout(readTimeout);
    return this;
  }

  @Override
  public HttpServerConfigurer configHttp1(java.util.function.Consumer<com.zernova.sdk.api.http.protocol.Http1ProtocolConfigurer> callback) {
    com.zernova.runtime.http.support.internal.HttpProtocolCompatibility.http1(callback);
    return this;
  }

  @Override
  public HttpServerConfigurer configHttp2(java.util.function.Consumer<com.zernova.sdk.api.http.protocol.Http2ProtocolConfigurer> callback) {
    com.zernova.runtime.http.support.internal.HttpProtocolCompatibility.http2(callback);
    return this;
  }
}
