/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.semantic.extension;

import static com.zernova.sdk.api.annotation.param.MediaType.APPLICATION_JSON;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.execution.OnError;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;
import com.zernova.sdk.api.annotation.execution.OnSuccess;
import com.zernova.sdk.api.annotation.semantics.connectivity.Endpoint;
import com.zernova.sdk.api.annotation.semantics.security.AccountId;
import com.zernova.sdk.api.annotation.semantics.security.Secret;
import com.zernova.sdk.api.annotation.semantics.security.TenantIdentifier;
import com.zernova.sdk.api.annotation.semantics.security.Username;

@MediaType(APPLICATION_JSON)
public class SemanticTermsSource extends Source<String, Void> {

  @Parameter
  @Endpoint
  private String endpoint;

  @OnSuccess
  private String onSuccess(@AccountId String accountId, @TenantIdentifier String tenantId) {
    return "";
  }

  @OnError
  private String onError(@Secret String secret, @Username String username) {
    return "";
  }

  @Override
  public void onStart(SourceCallback<String, Void> sourceCallback) throws ZernovaException {

  }

  @Override
  public void onStop() {

  }
}
