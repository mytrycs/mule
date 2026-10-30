/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/**
 * Wrapper of the HTTP part from SDK API, delegating to HTTP API.
 *
 * @moduleGraph
 * @since 4.10
 */
module com.zernova.runtime.http.support {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.http.api;
  requires com.zernova.sdk.api;

  exports com.zernova.runtime.http.support.api;
}
