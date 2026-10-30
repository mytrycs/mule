/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.clientcredentials;

import static java.util.Optional.ofNullable;
import com.zernova.runtime.extension.api.connectivity.oauth.ClientCredentialsState;

import java.util.Optional;

/**
 * Immutable implementation of {@link ClientCredentialsState}
 *
 * @since 4.2.1
 */
public class ImmutableClientCredentialsState implements ClientCredentialsState {

  private final String accessToken;
  private final String expiresIn;

  public ImmutableClientCredentialsState(String accessToken, String expiresIn) {
    this.accessToken = accessToken;
    this.expiresIn = expiresIn;
  }

  @Override
  public String getAccessToken() {
    return accessToken;
  }

  @Override
  public Optional<String> getExpiresIn() {
    return ofNullable(expiresIn);
  }
}
