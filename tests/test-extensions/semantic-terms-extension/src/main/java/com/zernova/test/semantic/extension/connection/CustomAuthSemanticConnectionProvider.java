/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.semantic.extension.connection;

import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.sdk.api.annotation.param.Parameter;
import com.zernova.sdk.api.annotation.semantics.connectivity.ExcludeFromConnectivitySchema;
import com.zernova.sdk.api.annotation.semantics.security.AccountId;
import com.zernova.sdk.api.annotation.semantics.security.ConnectionId;
import com.zernova.sdk.api.annotation.semantics.security.Secret;
import com.zernova.sdk.api.annotation.semantics.security.SecretToken;
import com.zernova.sdk.api.annotation.semantics.security.SecurityToken;
import com.zernova.sdk.api.annotation.semantics.security.SessionId;
import com.zernova.sdk.api.annotation.semantics.security.TenantIdentifier;
import com.zernova.sdk.api.annotation.semantics.security.TokenId;

@Alias("custom")
public class CustomAuthSemanticConnectionProvider extends SemanticTermsConnectionProvider {

  @Parameter
  @ConnectionId
  private String connectionId;

  @Parameter
  @TenantIdentifier
  private String tenantName;

  @Parameter
  @TokenId
  private String tokenId;

  @Parameter
  @SecurityToken
  private String securityToken;

  @Parameter
  @SecretToken
  private String secretToken;

  @Parameter
  @SessionId
  private String sessionId;

  @Parameter
  @AccountId
  private String accountId;

  @Parameter
  @Secret
  private byte[] certificate;

  @Parameter
  @Secret
  @ExcludeFromConnectivitySchema
  private String secretNumber;

}
