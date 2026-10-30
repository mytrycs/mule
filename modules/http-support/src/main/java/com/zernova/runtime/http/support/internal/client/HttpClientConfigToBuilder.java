/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.http.support.internal.client;

import com.zernova.runtime.api.tls.TlsContextFactory;
import com.zernova.runtime.http.api.client.HttpClientConfiguration;
import com.zernova.runtime.http.api.tcp.TcpClientSocketProperties;
import com.zernova.sdk.api.http.client.HttpClientConfigurer;
import com.zernova.sdk.api.http.client.proxy.ProxyConfigurer;
import com.zernova.sdk.api.http.tcp.TcpSocketPropertiesConfigurer;

import java.util.function.Consumer;

public record HttpClientConfigToBuilder(HttpClientConfiguration.Builder builder) implements HttpClientConfigurer {

  @Override
  public HttpClientConfigurer setTlsContextFactory(TlsContextFactory tlsContextFactory) {
    builder.setTlsContextFactory(tlsContextFactory);
    return this;
  }

  @Override
  public HttpClientConfigurer setMaxConnections(int maxConnections) {
    builder.setMaxConnections(maxConnections);
    return this;
  }

  @Override
  public HttpClientConfigurer setUsePersistentConnections(boolean usePersistentConnections) {
    builder.setUsePersistentConnections(usePersistentConnections);
    return this;
  }

  @Override
  public HttpClientConfigurer setConnectionIdleTimeout(int connectionIdleTimeout) {
    builder.setConnectionIdleTimeout(connectionIdleTimeout);
    return this;
  }

  @Override
  public HttpClientConfigurer setStreaming(boolean streaming) {
    builder.setStreaming(streaming);
    return this;
  }

  @Override
  public HttpClientConfigurer setResponseBufferSize(int responseBufferSize) {
    builder.setResponseBufferSize(responseBufferSize);
    return this;
  }

  @Override
  public HttpClientConfigurer setName(String name) {
    builder.setName(name);
    return this;
  }

  @Override
  public HttpClientConfigurer setDecompress(Boolean decompress) {
    builder.setDecompress(decompress);
    return this;
  }

  @Override
  public HttpClientConfigurer configClientSocketProperties(Consumer<TcpSocketPropertiesConfigurer> configCallback) {
    var propsBuilder = TcpClientSocketProperties.builder();
    var configurer = new TcpSocketPropertiesConfigurerToBuilder(propsBuilder);
    configCallback.accept(configurer);
    builder.setClientSocketProperties(propsBuilder.build());
    return this;
  }

  @Override
  public HttpClientConfigurer configProxy(Consumer<ProxyConfigurer> configCallback) {
    var configurer = new ProxyConfigImpl();
    configCallback.accept(configurer);
    builder.setProxyConfig(configurer.build());
    return this;
  }

  @Override
  public HttpClientConfigurer configHttp1(java.util.function.Consumer<com.zernova.sdk.api.http.protocol.Http1ProtocolConfigurer> callback) {
    com.zernova.runtime.http.support.internal.HttpProtocolCompatibility.http1(callback);
    return this;
  }

  @Override
  public HttpClientConfigurer configHttp2(java.util.function.Consumer<com.zernova.sdk.api.http.protocol.Http2ProtocolConfigurer> callback) {
    com.zernova.runtime.http.support.internal.HttpProtocolCompatibility.http2(callback);
    return this;
  }
}
