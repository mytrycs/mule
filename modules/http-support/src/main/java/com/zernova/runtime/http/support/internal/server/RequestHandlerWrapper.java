/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.support.internal.server;

import com.zernova.runtime.http.api.domain.request.HttpRequestContext;
import com.zernova.runtime.http.api.server.RequestHandler;
import com.zernova.runtime.http.api.server.async.HttpResponseReadyCallback;

public class RequestHandlerWrapper implements RequestHandler {

  private final com.zernova.sdk.api.http.server.RequestHandler delegate;

  public RequestHandlerWrapper(com.zernova.sdk.api.http.server.RequestHandler delegate) {
    this.delegate = delegate;
  }

  @Override
  public void handleRequest(HttpRequestContext requestContext, HttpResponseReadyCallback responseCallback) {
    delegate.handleRequest(new HttpRequestContextWrapper(requestContext), new HttpResponseReadyCallbackWrapper(responseCallback));
  }

  @Override
  public ClassLoader getContextClassLoader() {
    return delegate.getContextClassLoader();
  }
}
