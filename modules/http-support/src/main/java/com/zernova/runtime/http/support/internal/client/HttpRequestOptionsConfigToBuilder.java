/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.http.support.internal.client;

import com.zernova.runtime.http.api.client.HttpRequestOptionsBuilder;
import com.zernova.sdk.api.http.client.HttpRequestOptionsConfigurer;
import com.zernova.sdk.api.http.client.auth.HttpAuthenticationConfigurer;
import com.zernova.sdk.api.http.client.proxy.ProxyConfigurer;

import java.util.function.Consumer;

public record HttpRequestOptionsConfigToBuilder(HttpRequestOptionsBuilder builder) implements HttpRequestOptionsConfigurer {

  @Override
  public HttpRequestOptionsConfigurer setResponseTimeout(int responseTimeout) {
    builder.responseTimeout(responseTimeout);
    return this;
  }

  @Override
  public HttpRequestOptionsConfigurer setFollowsRedirect(boolean followsRedirect) {
    builder.followsRedirect(followsRedirect);
    return this;
  }

  @Override
  public HttpRequestOptionsConfigurer setAuthentication(Consumer<HttpAuthenticationConfigurer> authConfigCallback) {
    HttpAuthenticationConfigurerImpl configurer = new HttpAuthenticationConfigurerImpl();
    authConfigCallback.accept(configurer);
    builder.authentication(configurer.build());
    return this;
  }

  @Override
  public HttpRequestOptionsConfigurer setProxyConfig(Consumer<ProxyConfigurer> proxyConfigCallback) {
    ProxyConfigImpl configurer = new ProxyConfigImpl();
    proxyConfigCallback.accept(configurer);
    builder.proxyConfig(configurer.build());
    return this;
  }

  @Override
  public HttpRequestOptionsConfigurer setSendBodyAlways(boolean sendBodyAlways) {
    builder.sendBodyAlways(sendBodyAlways);
    return this;
  }
}
