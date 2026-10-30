/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.connection;

import static com.zernova.runtime.api.meta.model.connection.ConnectionManagementType.CACHED;
import static com.zernova.runtime.api.meta.model.connection.ConnectionManagementType.NONE;
import static com.zernova.runtime.api.meta.model.connection.ConnectionManagementType.POOLING;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.internal.connection.ConnectionUtils.getInjectionTarget;

import static java.util.Optional.empty;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.config.PoolingProfile;
import com.zernova.runtime.api.connection.CachedConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.api.connection.PoolingConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.meta.model.connection.ConnectionManagementType;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.core.internal.connection.adapter.XATransactionalConnectionProvider;

import java.util.Optional;

import org.slf4j.Logger;

import jakarta.inject.Inject;

/**
 * Base class for implementations of {@link ConnectionProviderWrapper}
 *
 * @param <C> the generic type of the connections that {@link #getDelegate()} produces
 * @since 4.3.0
 */
public abstract class AbstractConnectionProviderWrapper<C> implements ConnectionProviderWrapper<C> {

  private static final Logger LOGGER = getLogger(AbstractConnectionProviderWrapper.class);

  private final ConnectionProvider<C> delegate;

  private Object delegateForInjection;

  @Inject
  protected ZernovaContext muleContext;

  /**
   * Creates a new instance which wraps the {@code delegate}
   *
   * @param delegate the {@link ConnectionProvider} to be wrapped
   */
  public AbstractConnectionProviderWrapper(ConnectionProvider<C> delegate) {
    this.delegate = delegate;
  }

  @Override
  public C connect() throws ConnectionException {
    return ConnectionUtils.connect(delegate);
  }

  /**
   * Delegates the connection validation to the delegated {@link ConnectionProvider}
   *
   * @param connection a non {@code null} {@link C}.
   * @return the {@link ConnectionValidationResult} returned by the delegated {@link ConnectionProvider}
   */
  @Override
  public ConnectionValidationResult validate(C connection) {
    return delegate.validate(connection);
  }

  @Override
  public void disconnect(C connection) {
    delegate.disconnect(connection);
  }

  @Override
  public ConnectionProvider<C> getDelegate() {
    return delegate;
  }

  protected Object getDelegateForInjection() {
    if (delegateForInjection == null) {
      synchronized (this) {
        if (delegateForInjection == null) {
          delegateForInjection = getInjectionTarget(delegate);
        }
      }
    }
    return delegateForInjection;
  }

  /**
   * @return a {@link RetryPolicyTemplate}
   */
  @Override
  public RetryPolicyTemplate getRetryPolicyTemplate() {
    return ConnectionUtils.getRetryPolicyTemplate(getReconnectionConfig());
  }

  @Override
  public ConnectionManagementType getConnectionManagementType() {
    if (delegate instanceof ConnectionProviderWrapper) {
      return ((ConnectionProviderWrapper<C>) delegate).getConnectionManagementType();
    }
    ConnectionManagementType type = NONE;
    if (delegate instanceof PoolingConnectionProvider) {
      type = POOLING;
    } else if (delegate instanceof CachedConnectionProvider) {
      type = CACHED;
    }

    return type;
  }

  @Override
  public boolean supportsXa() {
    if (delegate instanceof ConnectionProviderWrapper) {
      return ((ConnectionProviderWrapper<C>) delegate).supportsXa();
    }

    return delegate instanceof XATransactionalConnectionProvider;
  }

  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(delegate, true, muleContext);
    initialiseIfNeeded(getRetryPolicyTemplate(), true, muleContext);
  }

  @Override
  public void start() throws ZernovaException {
    startIfNeeded(delegate);
    startIfNeeded(getRetryPolicyTemplate());
  }

  @Override
  public void stop() throws ZernovaException {
    stopIfNeeded(delegate);
    stopIfNeeded(getRetryPolicyTemplate());
  }

  @Override
  public void dispose() {
    disposeIfNeeded(delegate, LOGGER);
    disposeIfNeeded(getRetryPolicyTemplate(), LOGGER);
  }

  @Override
  public Optional<PoolingProfile> getPoolingProfile() {
    ConnectionProvider<C> delegate = getDelegate();
    return delegate instanceof ConnectionProviderWrapper
        ? ((ConnectionProviderWrapper) delegate).getPoolingProfile()
        : empty();
  }
}
