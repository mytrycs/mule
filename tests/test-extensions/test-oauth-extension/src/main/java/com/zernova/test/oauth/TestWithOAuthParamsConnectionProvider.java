/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.oauth;

import static com.zernova.runtime.api.connection.ConnectionValidationResult.success;
import static com.zernova.sdk.api.runtime.parameter.HttpParameterPlacement.BODY;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.connectivity.oauth.AuthorizationCode;
import com.zernova.runtime.extension.api.annotation.connectivity.oauth.OAuthParameter;
import com.zernova.runtime.extension.api.runtime.parameter.HttpParameterPlacement;
import com.zernova.sdk.api.annotation.param.Optional;

@AuthorizationCode(accessTokenUrl = TestOAuthConnectionProvider.ACCESS_TOKEN_URL,
    authorizationUrl = TestOAuthConnectionProvider.AUTH_URL,
    defaultScopes = TestOAuthConnectionProvider.DEFAULT_SCOPE)
@Alias("with-params-connection")
public class TestWithOAuthParamsConnectionProvider extends LegacyTestOAuthConnectionState
    implements ConnectionProvider<TestOAuthConnection> {

  public static final String ACCESS_TOKEN_URL = "accessTokenUrl";
  public static final String AUTH_URL = "authUrl";
  public static final String DEFAULT_SCOPE = "defaultScope";

  @OAuthParameter(requestAlias = "with_alias")
  String withAlias;

  @OAuthParameter()
  String defaultPlacement;

  @OAuthParameter(placement = HttpParameterPlacement.QUERY_PARAMS)
  String queryParam;

  @OAuthParameter(placement = HttpParameterPlacement.HEADERS)
  String header;

  @com.zernova.sdk.api.annotation.connectivity.oauth.OAuthParameter(placement = BODY)
  @Optional
  String bodyParameter;

  @Override
  public TestOAuthConnection connect() throws ConnectionException {
    return new TestOAuthConnection(this);
  }

  public void disconnect(TestOAuthConnection connection) {

  }

  @Override
  public ConnectionValidationResult validate(TestOAuthConnection connection) {
    return success();
  }
}
