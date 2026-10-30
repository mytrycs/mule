/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.connection;

import com.zernova.runtime.api.config.PoolingProfile;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.core.api.connector.ConnectionManager;
import com.zernova.runtime.core.api.retry.ReconnectionConfig;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;

/**
 * Interface for {@link ConnectionManager} implementations which expands its contract with non API functionality
 *
 * @since 4.0
 */
public interface ConnectionManagerAdapter extends ConnectionManager, Lifecycle {

  /**
   * Returns the {@link RetryPolicyTemplate} that should be applied to the given {@code connectionProvider}
   *
   * @param connectionProvider a {@link ConnectionProvider}
   * @param <C>                the generic type of the connection returned by the provider
   * @return a {@link RetryPolicyTemplate}
   */
  <C> RetryPolicyTemplate getRetryTemplateFor(ConnectionProvider<C> connectionProvider);

  /**
   * Returns the {@link ReconnectionConfig} that should be applied to the given {@code connectionProvider}
   *
   * @param connectionProvider a {@link ConnectionProvider}
   * @param <C>                the generic type of the connection returned by the provider
   * @return a {@link ReconnectionConfig}
   */
  <C> ReconnectionConfig getReconnectionConfigFor(ConnectionProvider<C> connectionProvider);

  /**
   * When no {@link PoolingProfile} is specified by the user the {@link ConnectionManagerAdapter} will provide the default one to
   * configure the pool of connections
   *
   * @return a {@link PoolingProfile}
   */
  PoolingProfile getDefaultPoolingProfile();
}
