/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ocs;

import static java.util.Optional.empty;

import com.zernova.oauth.client.api.state.ResourceOwnerOAuthContext;
import com.zernova.runtime.extension.api.connectivity.oauth.AuthorizationCodeState;
import com.zernova.runtime.oauth.api.PlatformManagedConnectionDescriptor;
import com.zernova.runtime.oauth.api.PlatformManagedOAuthDancer;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Implementation of {@link AbstractPlatformOAuthStateAdapter} that implements {@link AuthorizationCodeState}
 *
 * @since 4.3.0
 */
public class PlatformAuthorizationCodeStateAdapter extends AbstractPlatformOAuthStateAdapter implements AuthorizationCodeState {

  private final PlatformManagedConnectionDescriptor descriptor;

  public PlatformAuthorizationCodeStateAdapter(PlatformManagedOAuthDancer dancer,
                                               PlatformManagedConnectionDescriptor descriptor,
                                               Consumer<ResourceOwnerOAuthContext> onUpdate) {
    super(dancer, onUpdate);
    this.descriptor = descriptor;
  }

  @Override
  public Optional<String> getRefreshToken() {
    return empty();
  }

  @Override
  public String getResourceOwnerId() {
    return descriptor.getDisplayName();
  }

  @Override
  public Optional<String> getState() {
    return empty();
  }

  @Override
  public String getAuthorizationUrl() {
    return "";
  }

  @Override
  public String getAccessTokenUrl() {
    return "";
  }

  @Override
  public String getConsumerKey() {
    return "";
  }

  @Override
  public String getConsumerSecret() {
    return "";
  }

  @Override
  public Optional<String> getExternalCallbackUrl() {
    return empty();
  }
}
