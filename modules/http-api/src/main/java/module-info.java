/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Mule service that allows to create HTTP servers and clients.
 *
 * @moduleGraph
 * @since 4.5
 */
module com.zernova.runtime.http.api {

  requires transitive com.zernova.runtime.api;

  // For the deprecated API methods still relying on com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate
  requires com.zernova.runtime.core;
  requires com.github.benmanes.caffeine;

  exports com.zernova.runtime.http.api;
  exports com.zernova.runtime.http.api.client;
  exports com.zernova.runtime.http.api.client.auth;
  exports com.zernova.runtime.http.api.client.proxy;
  exports com.zernova.runtime.http.api.client.ws;
  exports com.zernova.runtime.http.api.domain;
  exports com.zernova.runtime.http.api.domain.entity;
  exports com.zernova.runtime.http.api.domain.entity.multipart;
  exports com.zernova.runtime.http.api.domain.message;
  exports com.zernova.runtime.http.api.domain.message.request;
  exports com.zernova.runtime.http.api.domain.message.response;
  exports com.zernova.runtime.http.api.domain.request;
  exports com.zernova.runtime.http.api.exception;
  exports com.zernova.runtime.http.api.server;
  exports com.zernova.runtime.http.api.server.async;
  exports com.zernova.runtime.http.api.server.ws;
  exports com.zernova.runtime.http.api.utils;
  exports com.zernova.runtime.http.api.sse;
  exports com.zernova.runtime.http.api.sse.client;
  exports com.zernova.runtime.http.api.sse.server;
  exports com.zernova.runtime.http.api.tcp;
  exports com.zernova.runtime.http.api.ws;
  exports com.zernova.runtime.http.api.ws.exception;

}
