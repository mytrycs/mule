/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.api.tcp;

/**
 * TCP client specific configuration.
 *
 * @since 4.0
 */
public interface TcpClientSocketProperties extends TcpSocketProperties {

  /**
   * @return a {@link TcpClientSocketPropertiesBuilder}.
   */
  static TcpClientSocketPropertiesBuilder builder() {
    return new TcpClientSocketPropertiesBuilder();
  }

  /**
   * Number of milliseconds to wait until an outbound connection to a remote server is successfully created. Defaults to 30
   * seconds.
   */
  Integer getConnectionTimeout();
}
