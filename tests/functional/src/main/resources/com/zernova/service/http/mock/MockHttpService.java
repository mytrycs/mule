/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.service.http.mock;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

import com.zernova.runtime.http.api.HttpService;
import com.zernova.runtime.http.api.client.HttpClient;
import com.zernova.runtime.http.api.client.HttpClientFactory;
import com.zernova.runtime.http.api.server.HttpServer;
import com.zernova.runtime.http.api.server.HttpServerFactory;
import com.zernova.runtime.http.api.utils.RequestMatcherRegistry;

public class MockHttpService implements HttpService {

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  public HttpServerFactory getServerFactory() {
    throw new UnsupportedOperationException();
  }

  public HttpClientFactory getClientFactory() {
    throw new UnsupportedOperationException();
  }

  public RequestMatcherRegistry.RequestMatcherRegistryBuilder getRequestMatcherRegistryBuilder() {
    throw new UnsupportedOperationException();
  }

}
