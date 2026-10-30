/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth;

import static java.util.Collections.unmodifiableMap;

import static com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ExtensionsOAuthUtils.refreshTokenIfNecessary;
import static com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ExtensionsOAuthUtils.validateOAuthConnection;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.core.api.retry.ReconnectionConfig;
import com.zernova.runtime.core.internal.connection.ConnectionProviderWrapper;
import com.zernova.runtime.core.internal.connection.ReconnectableConnectionProviderWrapper;
import com.zernova.oauth.client.api.state.ResourceOwnerOAuthContext;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Base class for an OAuth enabled {@link ConnectionProviderWrapper}.
 *
 * @param <C> the generic type of the returned connection
 * @since 4.3.0
 */
public abstract class BaseOAuthConnectionProviderWrapper<C> extends ReconnectableConnectionProviderWrapper<C> implements
    OAuthConnectionProviderWrapper<C> {

  protected final Map<Field, String> callbackValues;

  public BaseOAuthConnectionProviderWrapper(ConnectionProvider<C> delegate,
                                            ReconnectionConfig reconnectionConfig,
                                            Map<Field, String> callbackValues) {
    super(delegate, reconnectionConfig);
    this.callbackValues = unmodifiableMap(callbackValues);
  }

  @Override
  public ConnectionValidationResult validate(C connection) {
    return validateOAuthConnection(this, connection, getContext());
  }

  @Override
  public String getResourceOwnerId() {
    return getContext().getResourceOwnerId();
  }

  protected abstract ResourceOwnerOAuthContext getContext();
}
