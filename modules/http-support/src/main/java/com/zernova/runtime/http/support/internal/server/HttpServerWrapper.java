/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.http.support.internal.server;

import com.zernova.runtime.api.tls.TlsContextFactory;
import com.zernova.sdk.api.http.HttpConstants;
import com.zernova.sdk.api.http.server.HttpServer;
import com.zernova.sdk.api.http.server.RequestHandler;
import com.zernova.sdk.api.http.server.EndpointLifecycleHandler;
import com.zernova.sdk.api.http.server.ServerAddress;
import com.zernova.sdk.api.http.sse.server.SseClient;
import com.zernova.sdk.api.http.sse.server.SseEndpointManager;
import com.zernova.sdk.api.http.sse.server.SseRequestContext;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

public class HttpServerWrapper implements HttpServer {

  private final com.zernova.runtime.http.api.server.HttpServer delegate;
  private final ServerAddress serverAddress;

  public HttpServerWrapper(com.zernova.runtime.http.api.server.HttpServer delegate) {
    this.delegate = delegate;
    this.serverAddress = new ServerAddressWrapper(delegate.getServerAddress());
  }

  @Override
  public void start() throws com.zernova.runtime.api.exception.ZernovaException {
    try {
      delegate.start();
    } catch (IOException failure) {
      throw new com.zernova.runtime.api.exception.DefaultZernovaException("ZERNOVA HTTP server could not start", failure);
    }
  }

  @Override
  public void stop() {
    delegate.stop();
  }

  @Override
  public void dispose() {
    delegate.dispose();
  }

  @Override
  public ServerAddress getServerAddress() {
    return serverAddress;
  }

  @Override
  public HttpConstants.Protocol getProtocol() {
    return HttpConstants.Protocol.valueOf(delegate.getProtocol().name());
  }

  @Override
  public boolean isStopping() {
    return delegate.isStopping();
  }

  @Override
  public boolean isStopped() {
    return delegate.isStopped();
  }

  @Override
  public void enableTls(TlsContextFactory tlsContextFactory) {
    delegate.enableTls(tlsContextFactory);
  }

  @Override
  public void disableTls() {
    delegate.disableTls();
  }

  @Override
  public EndpointLifecycleHandler addRequestHandler(Collection<String> methods, String path, RequestHandler requestHandler) {
    return new EndpointAvailabilityHandlerWrapper(delegate.addRequestHandler(methods, path,
                                                                             new RequestHandlerWrapper(requestHandler)));
  }

  @Override
  public EndpointLifecycleHandler addRequestHandler(String path, RequestHandler requestHandler) {
    return new EndpointAvailabilityHandlerWrapper(delegate.addRequestHandler(path, new RequestHandlerWrapper(requestHandler)));
  }

  @Override
  public SseEndpointManager sse(String ssePath, Consumer<SseRequestContext> onRequest, Consumer<SseClient> onClient) {
    return new SseEndpointManagerWrapper(delegate.sse(ssePath,
                                                      ctx -> onRequest.accept(new SseRequestContextWrapper(ctx)),
                                                      sseClient -> onClient.accept(new SseClientWrapper(sseClient))));
  }
}
