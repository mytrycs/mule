/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.api.domain;

/**
 * Represents the HTTP message protocol.
 *
 * @since 4.0
 */
public enum HttpProtocol {

  HTTP_0_9("HTTP/0.9"), HTTP_1_0("HTTP/1.0"), HTTP_1_1("HTTP/1.1");

  private final String protocolName;

  HttpProtocol(String protocolName) {
    this.protocolName = protocolName;
  }

  public String asString() {
    return protocolName;
  }
}
