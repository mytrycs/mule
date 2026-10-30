/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.oauth;

import static com.zernova.runtime.api.connection.ConnectionValidationResult.failure;
import static com.zernova.runtime.api.connection.ConnectionValidationResult.success;

import com.zernova.runtime.api.connection.CachedConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.connectivity.oauth.AccessTokenExpiredException;
import com.zernova.sdk.api.annotation.connectivity.oauth.AuthorizationCode;

@AuthorizationCode(accessTokenUrl = TestOAuthConnectionProvider.ACCESS_TOKEN_URL,
    authorizationUrl = TestOAuthConnectionProvider.AUTH_URL)
@Alias("refresh-validation")
public class TestOAuthRefreshValidationProvider extends LegacyTestOAuthConnectionState
    implements CachedConnectionProvider<TestOAuthConnection> {

  public static int TIMES_REFRESH_IS_NEEDED = 0;

  public static final String ACCESS_TOKEN_URL = "accessTokenUrl";
  public static final String AUTH_URL = "authUrl";
  public static final String DEFAULT_SCOPE = "defaultScope";

  @Override
  public TestOAuthConnection connect() throws ConnectionException {
    return new TestOAuthConnection(this);
  }

  public void disconnect(TestOAuthConnection connection) {

  }

  @Override
  public ConnectionValidationResult validate(TestOAuthConnection connection) {
    if (TIMES_REFRESH_IS_NEEDED > 0) {
      TIMES_REFRESH_IS_NEEDED--;
      return failure("The token needs to be refreshed", new AccessTokenExpiredException());
    }
    return success();
  }

}
