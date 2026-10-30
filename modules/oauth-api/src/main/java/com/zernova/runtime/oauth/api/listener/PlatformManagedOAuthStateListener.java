/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.oauth.api.listener;

import com.zernova.api.annotation.Experimental;
import com.zernova.api.annotation.NoImplement;
import com.zernova.oauth.client.api.listener.OAuthStateListener;
import com.zernova.oauth.client.api.state.ResourceOwnerOAuthContext;

/**
 * Allows to get notified about certain events related to Platform Managed OAuth tokens
 * <p>
 * Platform Managed OAuth is an experimental feature. It will only be enabled on selected environments and scenarios. Backwards
 * compatibility is not guaranteed.
 *
 * @since 4.3.0
 */
@Experimental
@NoImplement
public interface PlatformManagedOAuthStateListener extends OAuthStateListener {

  /**
   * Invoked each time an access token has been obtained from the platform
   *
   * @param context the resulting {@link ResourceOwnerOAuthContext}
   */
  default void onAccessToken(ResourceOwnerOAuthContext context) {

  }

  /**
   * Invoked each time a refresh token operation has been completed successfully
   *
   * @param context the resulting {@link ResourceOwnerOAuthContext}
   */
  default void onTokenRefreshed(ResourceOwnerOAuthContext context) {

  }
}
