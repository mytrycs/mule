/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.http;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.http.api.HttpService;
import com.zernova.runtime.http.api.client.HttpClient;
import com.zernova.runtime.http.api.client.HttpClientConfiguration;
import com.zernova.runtime.http.api.client.HttpClientFactory;
import com.zernova.runtime.http.api.client.HttpRequestOptions;
import com.zernova.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import com.zernova.runtime.http.api.domain.entity.EmptyHttpEntity;
import com.zernova.runtime.http.api.domain.entity.HttpEntity;
import com.zernova.runtime.http.api.domain.message.request.HttpRequest;
import com.zernova.runtime.http.api.domain.message.response.HttpResponse;
import com.zernova.runtime.http.api.server.HttpServerFactory;
import com.zernova.runtime.http.api.utils.RequestMatcherRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * This HTTP service fake implementation validates resource access via it's clients send method. It allows testing which resources
 * will be accessible to a service classloader.
 */
public class ResourceHttpService implements HttpService {

  @Override
  public HttpServerFactory getServerFactory() {
    return null;
  }

  @Override
  public HttpClientFactory getClientFactory() {
    return new HttpClientFactory() {

      @Override
      public HttpClient create(HttpClientConfiguration configuration) {
        return new HttpClient() {

          @Override
          public void start() {

          }

          @Override
          public void stop() {

          }

          /**
           * Loads MANIFEST resources and gets their Bundle-Description.
           *
           * @param request the {@link HttpRequest} to send (the entities bytes will be treated as the resource name)
           * @param options the {@link HttpRequestOptions} to use (ignored)
           * @return the Manifest Bundle-Descriptor or an empty String if not found.
           * @throws IOException
           * @throws TimeoutException
           */
          @Override
          public HttpResponse send(HttpRequest request, HttpRequestOptions options)
              throws IOException, TimeoutException {
            String resource = new String(request.getEntity().getBytes());
            InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream(resource);
            String body = null;
            if (resourceStream != null) {
              Properties properties = new Properties();
              try {
                properties.load(resourceStream);
              } catch (IOException e) {
                throw new ZernovaRuntimeException(e);
              }
              body = properties.getProperty("Bundle-Description");
            }
            return HttpResponse.builder()
                .entity(resolveEntity(body))
                .build();
          }

          @Override
          public CompletableFuture<HttpResponse> sendAsync(HttpRequest request, HttpRequestOptions options) {
            return null;
          }
        };
      }
    };
  }

  @Override
  public RequestMatcherRegistry.RequestMatcherRegistryBuilder getRequestMatcherRegistryBuilder() {
    return null;
  }

  private HttpEntity resolveEntity(String bundleDescription) {
    return bundleDescription == null ? new EmptyHttpEntity() : new ByteArrayHttpEntity(bundleDescription.getBytes());
  }

  @Override
  public String getName() {
    return "Resource HTTP Service";
  }
}
