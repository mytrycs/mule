/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.connection;

import static java.util.Optional.empty;

import com.zernova.runtime.api.config.HasPoolingProfile;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.meta.model.connection.ConnectionManagementType;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.core.internal.retry.HasReconnectionConfig;

import java.util.Optional;

/**
 * Base contract for wrappers of {@link ConnectionProvider} instances
 *
 * @param <C> the generic type of the connections that {@link #getDelegate()} produces
 * @since 4.3.0
 */
public interface ConnectionProviderWrapper<C>
    extends ConnectionProvider<C>, HasPoolingProfile, HasReconnectionConfig, HasDelegate<C>, Lifecycle {

  RetryPolicyTemplate getRetryPolicyTemplate();

  /**
   * @return the {@link ConnectionManagementType} of the delegate {@link ConnectionProvider}.
   */
  ConnectionManagementType getConnectionManagementType();

  /**
   * @return {@code true} if the delegate {@link ConnectionProvider} may be used in an XA transaction.
   * @since 4.10
   */
  boolean supportsXa();

  /**
   * @return The name of the config that owns this {@link ConnectionProvider}.
   *
   * @since 4.5
   */
  default Optional<String> getOwnerConfigName() {
    ConnectionProvider<C> delegate = getDelegate();
    if (delegate instanceof ConnectionProviderWrapper) {
      return ((ConnectionProviderWrapper<C>) delegate).getOwnerConfigName();
    }
    return empty();
  }
}
