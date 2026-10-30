/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Mule service that provides OAuth authentication support.
 *
 * @moduleGraph
 * @since 4.5
 */
module com.zernova.runtime.oauth.api {

  requires transitive com.zernova.runtime.api;
  requires transitive com.zernova.oauth.client.api;
  requires com.zernova.runtime.http.api;

  exports com.zernova.runtime.oauth.api;
  exports com.zernova.runtime.oauth.api.builder;
  exports com.zernova.runtime.oauth.api.listener;
  exports com.zernova.runtime.oauth.api.state;

  opens com.zernova.runtime.oauth.api.state to
      kryo.shaded;
}
