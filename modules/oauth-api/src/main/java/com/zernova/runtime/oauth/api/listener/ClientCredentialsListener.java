/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.oauth.api.listener;

import com.zernova.oauth.client.api.state.ResourceOwnerOAuthContext;

/**
 * Allows to get notified about certain events related to an OAuth dance with Client Credentials grant type
 *
 * @since 4.2.1
 * @deprecated since 4.2.2. Use {@link com.zernova.oauth.client.api.listener.ClientCredentialsListener} instead
 */
@Deprecated
public interface ClientCredentialsListener extends com.zernova.oauth.client.api.listener.ClientCredentialsListener,
    OAuthStateListener {

  /**
   * Invoked each time a refresh token operation has been completed successfully
   *
   * @param context the resulting {@link ResourceOwnerOAuthContext}
   */
  @Override
  void onTokenRefreshed(ResourceOwnerOAuthContext context);

  default void onTokenRefreshed(com.zernova.runtime.oauth.api.state.ResourceOwnerOAuthContext context) {}
}
