/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.oauth;

import static com.zernova.runtime.extension.api.values.ValueBuilder.getValuesFor;

import com.zernova.runtime.api.value.Value;
import com.zernova.runtime.extension.api.values.ValueProvider;
import com.zernova.runtime.extension.api.values.ValueResolvingException;
import com.zernova.sdk.api.connectivity.oauth.AccessTokenExpiredException;

import java.util.Set;

public class OAuthValuesProvider implements ValueProvider {

  @com.zernova.sdk.api.annotation.param.Connection
  private TestOAuthConnection testOAuthConnection;

  @Override
  public Set<Value> resolve() throws ValueResolvingException {
    if (!testOAuthConnection.getState().getState().getAccessToken().contains("refresh")) {
      throw new AccessTokenExpiredException();
    }
    return getValuesFor("Option1", "Option2", "Option3");
  }
}
