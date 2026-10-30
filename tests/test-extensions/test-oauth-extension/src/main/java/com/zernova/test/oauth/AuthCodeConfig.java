/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.oauth;

import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.Sources;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.connectivity.oauth.AuthorizationCodeState;
import com.zernova.sdk.api.connectivity.oauth.AuthCodeRequest;

import java.util.LinkedList;
import java.util.List;

@Configuration(name = "auth-code")
@ConnectionProviders({TestOAuthConnectionProvider.class, ScopelessOAuthConnectionProvider.class,
    TestOAuthRefreshValidationConnectionProvider.class, TestWithOAuthParamsConnectionProvider.class,
    TestAuthorizationCodeWithCredentialsPlacementConnectionProvider.class,
    TestAuthorizationCodeDoNotIncludeRedirectUriParamConnectionProvider.class, SdkTestOAuthConnectionProvider.class,
    SdkTestOAuthClientCredentialProvider.class})
@Operations({TestOAuthOperations.class, CallbackOperations.class})
@Sources({TestOAuthRefreshPollingSource.class, TestOAuthRefreshSource.class})
public class AuthCodeConfig {

  private List<AuthCodeRequest> capturedAuthCodeRequests = new LinkedList<>();
  private List<AuthorizationCodeState> capturedAuthCodeStates = new LinkedList<>();

  public List<AuthCodeRequest> getCapturedAuthCodeRequests() {
    return capturedAuthCodeRequests;
  }

  public List<AuthorizationCodeState> getCapturedAuthCodeStates() {
    return capturedAuthCodeStates;
  }
}
