/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.support.internal.message.sdktomule;

import com.zernova.runtime.api.util.MultiMap;
import com.zernova.runtime.http.api.domain.entity.HttpEntity;
import com.zernova.runtime.http.api.domain.message.response.HttpResponse;

import java.util.Collection;

public class HttpResponseWrapper implements HttpResponse {

  private final com.zernova.sdk.api.http.domain.message.response.HttpResponse sdkResponse;

  public HttpResponseWrapper(com.zernova.sdk.api.http.domain.message.response.HttpResponse sdkResponse) {
    this.sdkResponse = sdkResponse;
  }

  @Override
  public int getStatusCode() {
    return sdkResponse.getStatusCode();
  }

  @Override
  public String getReasonPhrase() {
    return sdkResponse.getReasonPhrase();
  }

  @Override
  public HttpEntity getEntity() {
    return new HttpEntityWrapper(sdkResponse.getEntity());
  }

  @Override
  public Collection<String> getHeaderNames() {
    return sdkResponse.getHeaderNames();
  }

  @Override
  public String getHeaderValue(String headerName) {
    return sdkResponse.getHeaderValue(headerName);
  }

  @Override
  public String getHeaderValueIgnoreCase(String headerName) {
    return sdkResponse.getHeaderValue(headerName);
  }

  @Override
  public Collection<String> getHeaderValues(String headerName) {
    return sdkResponse.getHeaderValues(headerName);
  }

  @Override
  public Collection<String> getHeaderValuesIgnoreCase(String headerName) {
    return sdkResponse.getHeaderValues(headerName);
  }

  @Override
  public MultiMap<String, String> getHeaders() {
    return sdkResponse.getHeaders();
  }
}
