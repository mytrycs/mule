/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.api.domain.request;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.http.api.server.HttpServer;

import java.net.InetSocketAddress;
import java.security.cert.Certificate;

/**
 * Representation of all client related data concerning an {@link HttpServer}.
 *
 * @since 4.0
 */
@NoImplement
public interface ClientConnection {

  /**
   * @return the host address from the client
   */
  InetSocketAddress getRemoteHostAddress();

  /**
   * @return the client certificate provided during the TLS client authentication, returns null if the TLS connection didn't
   *         require client authentication or if the connection is not using TLS.
   */
  Certificate getClientCertificate();

}
