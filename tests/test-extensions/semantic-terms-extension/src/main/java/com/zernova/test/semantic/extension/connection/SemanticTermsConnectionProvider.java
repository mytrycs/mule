/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.semantic.extension.connection;

import static com.zernova.runtime.api.connection.ConnectionValidationResult.success;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.annotation.semantics.connectivity.Domain;
import com.zernova.sdk.api.annotation.semantics.connectivity.Endpoint;
import com.zernova.sdk.api.annotation.semantics.connectivity.Host;
import com.zernova.sdk.api.annotation.semantics.connectivity.Port;
import com.zernova.sdk.api.annotation.semantics.connectivity.Url;
import com.zernova.sdk.api.annotation.semantics.connectivity.UrlPath;
import com.zernova.test.semantic.extension.ProxyConfiguration;

public abstract class SemanticTermsConnectionProvider implements ConnectionProvider<SemanticConnection> {

  @Parameter
  @Host
  private String host;

  @Parameter
  @Port
  private int port;

  @Parameter
  @Url
  private String url;

  @Parameter
  @Domain
  private String domain;

  @Parameter
  @UrlPath
  private String urlPath;

  @Parameter
  private ProxyConfiguration proxyConfiguration;

  @Parameter
  @Endpoint
  private String endpoint;

  @Override
  public SemanticConnection connect() throws ConnectionException {
    return new SemanticConnection();
  }

  @Override
  public void disconnect(SemanticConnection semanticConnection) {

  }

  @Override
  public ConnectionValidationResult validate(SemanticConnection semanticConnection) {
    return success();
  }
}
