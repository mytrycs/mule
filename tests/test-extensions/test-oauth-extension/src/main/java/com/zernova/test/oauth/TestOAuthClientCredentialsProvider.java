/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.oauth;

import static com.zernova.runtime.api.connection.ConnectionValidationResult.success;
import static com.zernova.runtime.extension.api.runtime.parameter.HttpParameterPlacement.HEADERS;
import static com.zernova.sdk.api.security.CredentialsPlacement.QUERY_PARAMS;
import static com.zernova.sdk.api.runtime.parameter.HttpParameterPlacement.BODY;
import static com.zernova.test.oauth.TestOAuthConnectionProvider.ACCESS_TOKEN_URL;
import static com.zernova.test.oauth.TestOAuthConnectionProvider.DEFAULT_SCOPE;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.connectivity.oauth.OAuthParameter;
import com.zernova.runtime.extension.api.annotation.param.NullSafe;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.connectivity.oauth.ClientCredentialsState;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthState;
import com.zernova.sdk.api.annotation.connectivity.oauth.ClientCredentials;

import java.util.Map;

@ClientCredentials(tokenUrl = ACCESS_TOKEN_URL, defaultScopes = DEFAULT_SCOPE, credentialsPlacement = QUERY_PARAMS)
@Alias("client-credentials")
public class TestOAuthClientCredentialsProvider extends LegacyTestOAuthConnectionState
    implements ConnectionProvider<TestOAuthConnection> {

  private ClientCredentialsState state;

  @OAuthParameter(placement = HEADERS)
  @Optional
  private String knownCustomHeader;

  @com.zernova.sdk.api.annotation.connectivity.oauth.OAuthParameter(placement = BODY)
  @Optional
  private String bodyParameter;

  @OAuthParameter(placement = HEADERS)
  @Optional
  @NullSafe
  private Map<String, String> randomHeaders;

  @Override
  public TestOAuthConnection connect() throws ConnectionException {
    return new TestOAuthConnection(this);
  }

  public void disconnect(TestOAuthConnection connection) {

  }

  @Override
  public OAuthState getState() {
    return state;
  }

  @Override
  public ConnectionValidationResult validate(TestOAuthConnection connection) {
    return success();
  }
}
