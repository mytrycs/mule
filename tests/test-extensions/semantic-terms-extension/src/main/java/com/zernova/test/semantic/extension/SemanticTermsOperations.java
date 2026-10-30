/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.semantic.extension;

import static com.zernova.sdk.api.annotation.param.MediaType.APPLICATION_JSON;

import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.display.Password;
import com.zernova.sdk.api.annotation.semantics.connectivity.Host;
import com.zernova.sdk.api.annotation.semantics.connectivity.UrlPath;
import com.zernova.sdk.api.annotation.semantics.security.AccountId;
import com.zernova.sdk.api.annotation.semantics.security.TenantIdentifier;

public class SemanticTermsOperations {

  @MediaType(APPLICATION_JSON)
  public String send(@Host String host, @Password String password, @AccountId String accountId) {
    return "";
  }

  @MediaType(APPLICATION_JSON)
  public String redirect(@UrlPath String path) {
    return "";
  }

  @MediaType(APPLICATION_JSON)
  public String bill(@TenantIdentifier String tenant, @AccountId String account) {
    return "";
  }
}
