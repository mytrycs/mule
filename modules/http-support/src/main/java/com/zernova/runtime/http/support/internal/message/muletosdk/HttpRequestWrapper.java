/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.support.internal.message.muletosdk;

import com.zernova.runtime.api.util.MultiMap;
import com.zernova.sdk.api.http.domain.HttpProtocolVersion;
import com.zernova.sdk.api.http.domain.entity.HttpEntity;
import com.zernova.sdk.api.http.domain.message.request.HttpRequest;

import java.net.URI;
import java.util.Collection;

public class HttpRequestWrapper implements HttpRequest {

  private final com.zernova.runtime.http.api.domain.message.request.HttpRequest muleRequest;

  public HttpRequestWrapper(com.zernova.runtime.http.api.domain.message.request.HttpRequest request) {
    this.muleRequest = request;
  }

  @Override
  public HttpProtocolVersion getProtocolVersion() {
    return HttpProtocolVersion.valueOf(muleRequest.getProtocol().name());
  }

  @Override
  public String getPath() {
    return muleRequest.getPath();
  }

  @Override
  public String getMethod() {
    return muleRequest.getMethod();
  }

  @Override
  public URI getUri() {
    return muleRequest.getUri();
  }

  @Override
  public MultiMap<String, String> getQueryParams() {
    return muleRequest.getQueryParams();
  }

  @Override
  public HttpEntity getEntity() {
    return new HttpEntityWrapper(muleRequest.getEntity());
  }

  @Override
  public Collection<String> getHeaderNames() {
    return muleRequest.getHeaderNames();
  }

  @Override
  public boolean containsHeader(String headerName) {
    return muleRequest.containsHeader(headerName);
  }

  @Override
  public String getHeaderValue(String headerName) {
    return muleRequest.getHeaderValue(headerName);
  }

  @Override
  public Collection<String> getHeaderValues(String headerName) {
    return muleRequest.getHeaderValues(headerName);
  }

  @Override
  public MultiMap<String, String> getHeaders() {
    return muleRequest.getHeaders();
  }
}
