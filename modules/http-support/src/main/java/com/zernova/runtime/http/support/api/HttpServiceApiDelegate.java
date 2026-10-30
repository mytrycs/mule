/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.http.support.api;

import com.zernova.runtime.http.api.HttpService;
import com.zernova.runtime.http.api.client.HttpClientConfiguration;
import com.zernova.runtime.http.api.server.HttpServerConfiguration;
import com.zernova.runtime.http.api.server.ServerCreationException;
import com.zernova.runtime.http.support.internal.client.HttpClientConfigToBuilder;
import com.zernova.runtime.http.support.internal.client.HttpClientWrapper;
import com.zernova.runtime.http.support.internal.message.HttpEntityFactoryImpl;
import com.zernova.runtime.http.support.internal.message.HttpRequestBuilderWrapper;
import com.zernova.runtime.http.support.internal.message.HttpResponseBuilderWrapper;
import com.zernova.runtime.http.support.internal.server.HttpServerConfigToBuilder;
import com.zernova.runtime.http.support.internal.server.HttpServerWrapper;
import com.zernova.sdk.api.http.client.ClientCreationException;
import com.zernova.sdk.api.http.client.HttpClient;
import com.zernova.sdk.api.http.client.HttpClientConfigurer;
import com.zernova.sdk.api.http.domain.entity.HttpEntityFactory;
import com.zernova.sdk.api.http.domain.message.request.HttpRequestBuilder;
import com.zernova.sdk.api.http.domain.message.response.HttpResponse;
import com.zernova.sdk.api.http.domain.message.response.HttpResponseBuilder;
import com.zernova.sdk.api.http.server.HttpServer;
import com.zernova.sdk.api.http.server.HttpServerConfigurer;

import java.util.Optional;
import java.util.function.Consumer;

import jakarta.inject.Inject;

public class HttpServiceApiDelegate implements com.zernova.sdk.api.http.HttpService {

  private final HttpEntityFactory httpEntityFactory = new HttpEntityFactoryImpl();

  private HttpService httpService;

  @Inject
  public void setHttpService(Optional<HttpService> httpService) {
    this.httpService = httpService.orElse(null);
  }

  @Override
  public HttpClient client(Consumer<HttpClientConfigurer> configCallback) throws ClientCreationException {
    if (httpService == null) {
      throw new ClientCreationException("There is no implementation of HttpService available");
    }
    var builder = new HttpClientConfiguration.Builder();
    var configurer = new HttpClientConfigToBuilder(builder);
    configCallback.accept(configurer);
    HttpClientConfiguration configuration = builder.build();
    try {
      return new HttpClientWrapper(httpService.getClientFactory().create(configuration));
    } catch (Exception e) {
      throw new ClientCreationException("Couldn't create client", e);
    }
  }

  @Override
  public HttpServer server(Consumer<HttpServerConfigurer> configCallback)
      throws com.zernova.sdk.api.http.server.ServerCreationException {
    if (httpService == null) {
      throw new com.zernova.sdk.api.http.server.ServerCreationException("There is no implementation of HttpService available");
    }
    var builder = new HttpServerConfiguration.Builder();
    var configurer = new HttpServerConfigToBuilder(builder);
    configCallback.accept(configurer);
    HttpServerConfiguration configuration = builder.build();
    try {
      return new HttpServerWrapper(httpService.getServerFactory().create(configuration));
    } catch (ServerCreationException e) {
      throw new com.zernova.sdk.api.http.server.ServerCreationException(e.getMessage(), e);
    }
  }

  @Override
  public HttpResponseBuilder responseBuilder() {
    return new HttpResponseBuilderWrapper();
  }

  @Override
  public HttpResponseBuilder responseBuilder(HttpResponse original) {
    return responseBuilder().statusCode(original.getStatusCode()).reasonPhrase(original.getReasonPhrase());
  }

  @Override
  public HttpRequestBuilder requestBuilder() {
    return new HttpRequestBuilderWrapper();
  }

  @Override
  public HttpRequestBuilder requestBuilder(boolean preserveHeaderCase) {
    return new HttpRequestBuilderWrapper(preserveHeaderCase);
  }

  @Override
  public HttpEntityFactory entityFactory() {
    return httpEntityFactory;
  }
}
