/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.authcode;

import static com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ExtensionsOAuthUtils.toAuthorizationCodeState;

import com.zernova.runtime.extension.api.connectivity.oauth.AuthorizationCodeState;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.exception.TokenInvalidatedException;
import com.zernova.oauth.client.api.AuthorizationCodeOAuthDancer;
import com.zernova.oauth.client.api.listener.AuthorizationCodeListener;
import com.zernova.oauth.client.api.state.ResourceOwnerOAuthContext;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An implementation of {@link AuthorizationCodeListener} which registers an {@link AuthorizationCodeListener} in order to get
 * updated state when a refresh token operation is completed or the resource is simply re-authorized.
 */
public class UpdatingAuthorizationCodeState
    implements AuthorizationCodeState, com.zernova.sdk.api.connectivity.oauth.AuthorizationCodeState {

  private AuthorizationCodeState delegate;
  private boolean invalidated = false;
  private Supplier<Boolean> getInvalidationStatusFromTokenStore;
  private final boolean forceInvalidateStatusRetrieval;

  public UpdatingAuthorizationCodeState(AuthorizationCodeConfig config,
                                        AuthorizationCodeOAuthDancer dancer,
                                        ResourceOwnerOAuthContext initialContext,
                                        Consumer<ResourceOwnerOAuthContext> onUpdate, boolean forceInvalidateStatusRetrieval) {
    delegate = toAuthorizationCodeState(config, initialContext);
    dancer.addListener(initialContext.getResourceOwnerId(), new AuthorizationCodeListener() {

      @Override
      public void onAuthorizationCompleted(ResourceOwnerOAuthContext context) {
        update(context);
      }

      @Override
      public void onTokenRefreshed(ResourceOwnerOAuthContext context) {
        update(context);
      }

      @Override
      public void onTokenInvalidated() {
        invalidated = true;
      }

      private void update(ResourceOwnerOAuthContext context) {
        delegate = toAuthorizationCodeState(config, context);
        invalidated = false;
        onUpdate.accept(context);
      }
    });
    this.forceInvalidateStatusRetrieval = forceInvalidateStatusRetrieval;
    getInvalidationStatusFromTokenStore = () -> dancer.getInvalidateFromTokensStore(getResourceOwnerId());
  }

  @Override
  public String getAccessToken() {
    checkTokenNotInvalidated();
    if (forceInvalidateStatusRetrieval) {
      this.invalidated = getInvalidationStatusFromTokenStore.get();
      checkTokenNotInvalidated();
    }
    return delegate.getAccessToken();
  }

  private void checkTokenNotInvalidated() {
    if (invalidated) {
      throw new TokenInvalidatedException(
                                          "OAuth token for resource owner id " + delegate.getResourceOwnerId()
                                              + " has been invalidated");
    }
  }

  @Override
  public Optional<String> getRefreshToken() {
    return delegate.getRefreshToken();
  }

  @Override
  public String getResourceOwnerId() {
    return delegate.getResourceOwnerId();
  }

  @Override
  public Optional<String> getExpiresIn() {
    return delegate.getExpiresIn();
  }

  @Override
  public Optional<String> getState() {
    return delegate.getState();
  }

  @Override
  public String getAuthorizationUrl() {
    return delegate.getAuthorizationUrl();
  }

  @Override
  public String getAccessTokenUrl() {
    return delegate.getAccessTokenUrl();
  }

  @Override
  public String getConsumerKey() {
    return delegate.getConsumerKey();
  }

  @Override
  public String getConsumerSecret() {
    return delegate.getConsumerSecret();
  }

  @Override
  public Optional<String> getExternalCallbackUrl() {
    return delegate.getExternalCallbackUrl();
  }
}
