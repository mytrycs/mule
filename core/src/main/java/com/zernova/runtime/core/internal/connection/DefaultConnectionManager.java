/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.connection;

import static com.zernova.runtime.api.connection.ConnectionValidationResult.failure;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.assertNotStopping;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.retry.ReconnectionConfig.defaultReconnectionConfig;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.config.PoolingProfile;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionHandler;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.util.Reference;
import com.zernova.runtime.core.api.Injector;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.connector.ConnectionManager;
import com.zernova.runtime.core.api.lifecycle.LifecycleState;
import com.zernova.runtime.core.api.retry.ReconnectionConfig;
import com.zernova.runtime.core.api.retry.policy.NoRetryPolicyTemplate;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;

import jakarta.inject.Inject;

/**
 * Implementation of {@link ConnectionManager} which manages connections opened on a specific application.
 *
 * @since 4.0
 */
public final class DefaultConnectionManager implements ConnectionManagerAdapter {

  private static final Logger LOGGER = getLogger(DefaultConnectionManager.class);

  private final Map<Reference<Object>, ConnectionManagementStrategy> connections = new HashMap<>();
  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final Lock readLock = readWriteLock.readLock();
  private final Lock writeLock = readWriteLock.writeLock();
  private final Injector injector;
  private final LifecycleState deploymentLifecycleState;
  private final RetryPolicyTemplate retryPolicyTemplate;
  private final PoolingProfile defaultPoolingProfile;
  private final ReconnectionConfig defaultReconnectionConfig = defaultReconnectionConfig();

  private ConnectionManagementStrategyFactory managementStrategyFactory;

  @Inject
  private Optional<XAConnectionManagementStrategyFactory> xaConnectionManagementStrategyFactory;

  @Inject
  private FeatureFlaggingService featureFlaggingService;

  /**
   * Creates a new instance
   *
   * @param muleContext the {@link ZernovaContext} of the owned application
   */
  @Inject
  public DefaultConnectionManager(ZernovaContext muleContext) {
    this.deploymentLifecycleState = muleContext.getLifecycleManager().getState();
    this.injector = muleContext.getInjector();
    this.defaultPoolingProfile = new PoolingProfile();
    this.retryPolicyTemplate = new NoRetryPolicyTemplate();
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalStateException if invoked while the {@link #muleContext} is stopped or stopping
   */
  @Override
  public <C> void bind(Object owner, ConnectionProvider<C> connectionProvider) {
    assertNotStopping(deploymentLifecycleState, "Mule is shutting down... cannot bind new connections");

    connectionProvider = new DefaultConnectionProviderWrapper<>(connectionProvider, injector);
    ConnectionManagementStrategy<C> managementStrategy =
        managementStrategyFactory.getStrategy(connectionProvider, featureFlaggingService);

    ConnectionManagementStrategy<C> previous;

    writeLock.lock();
    try {
      previous = connections.put(new Reference<>(owner), managementStrategy);
    } finally {
      writeLock.unlock();
    }

    if (previous != null) {
      close(previous);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <C> RetryPolicyTemplate getRetryTemplateFor(ConnectionProvider<C> connectionProvider) {
    return connectionProvider instanceof ConnectionProviderWrapper
        ? ((ConnectionProviderWrapper) connectionProvider).getRetryPolicyTemplate()
        : defaultReconnectionConfig.getRetryPolicyTemplate();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <C> ReconnectionConfig getReconnectionConfigFor(ConnectionProvider<C> connectionProvider) {
    return connectionProvider instanceof ConnectionProviderWrapper
        ? ((ConnectionProviderWrapper) connectionProvider).getReconnectionConfig().orElse(defaultReconnectionConfig)
        : defaultReconnectionConfig;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <C> ConnectionValidationResult testConnectivity(ConnectionProvider<C> connectionProvider) {
    return doTestConnectivity(() -> {
      try {
        return doTestConnectivity(connectionProvider,
                                  managementStrategyFactory.getStrategy(connectionProvider, featureFlaggingService)
                                      .getConnectionHandler());
      } catch (ConnectionException e) {
        return failure(e.getMessage(), e.getErrorType().orElse(null), e);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <C> ConnectionValidationResult testConnectivity(C connection, ConnectionHandler<C> connectionHandler) {
    if (!(connectionHandler instanceof ConnectionHandlerAdapter)) {
      throw new IllegalArgumentException("ConnectionHandler was not produced through this manager");
    }

    return ((ConnectionHandlerAdapter) connectionHandler).getConnectionProvider().validate(connection);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConnectionValidationResult testConnectivity(ConfigurationInstance configurationInstance)
      throws IllegalArgumentException {

    if (!configurationInstance.getConnectionProvider().isPresent()) {
      throw new IllegalArgumentException("The component does not support connectivity testing");
    }

    return doTestConnectivity(() -> {
      ConnectionProvider<Object> connectionProvider = configurationInstance.getConnectionProvider().get();
      final Object config = configurationInstance.getValue();
      ConnectionHandler<Object> connectionHandler;
      try {
        readLock.lock();
        try {
          connectionHandler = hasBinding(config)
              ? getConnection(config)
              : managementStrategyFactory.getStrategy(connectionProvider, featureFlaggingService).getConnectionHandler();
        } finally {
          readLock.unlock();
        }
      } catch (ConnectionException e) {
        return failure(e.getMessage(), e.getErrorType().orElse(null), e);
      }

      return doTestConnectivity(connectionProvider, connectionHandler);
    });

  }

  @Override
  public ConnectionValidationResult testConnectivity(ConfigurationInstance configurationInstance, boolean force)
      throws IllegalArgumentException {
    return testConnectivity(configurationInstance);
  }

  private ConnectionValidationResult doTestConnectivity(Callable<ConnectionValidationResult> callable) {
    try {
      return callable.call();
    } catch (Exception e) {
      return failure("Exception was found trying to test connectivity", e);
    }
  }

  private <C> ConnectionValidationResult doTestConnectivity(ConnectionProvider<C> connectionProvider,
                                                            ConnectionHandler<C> connectionHandler)
      throws Exception {
    try {
      return connectionProvider.validate(connectionHandler.getConnection());
    } catch (ConnectionException e) {
      return failure(e.getMessage(), e.getErrorType().orElse(null), e);
    } finally {
      if (connectionHandler != null) {
        connectionHandler.release();
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasBinding(Object config) {
    return connections.containsKey(new Reference<>(config));
  }

  /**
   * {@inheritDoc}
   */
  // TODO: MULE-9082
  @Override
  public void unbind(Object config) {
    ConnectionManagementStrategy managementStrategy;
    writeLock.lock();
    try {
      managementStrategy = connections.remove(new Reference<>(config));
    } finally {
      writeLock.unlock();
    }

    if (managementStrategy != null) {
      close(managementStrategy);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <C> ConnectionHandler<C> getConnection(Object config) throws ConnectionException {
    ConnectionManagementStrategy<C> handlingStrategy = null;
    readLock.lock();
    try {
      handlingStrategy = connections.get(new Reference<>(config));
    } finally {
      readLock.unlock();
    }

    if (handlingStrategy == null) {
      throw new ConnectionException("No ConnectionProvider has been registered for owner " + config);
    }

    return handlingStrategy.getConnectionHandler();
  }

  /**
   * Breaks all bindings and closes all connections
   *
   * @throws ZernovaException in case of error.
   */
  @Override
  public void stop() throws ZernovaException {
    writeLock.lock();
    try {
      connections.values().stream().forEach(this::close);
      connections.clear();
    } finally {
      writeLock.unlock();
    }
  }

  // TODO: MULE-9082
  private void close(ConnectionManagementStrategy managementStrategy) {
    try {
      managementStrategy.close();
    } catch (Exception e) {
      LOGGER.warn("An error was found trying to release connections", e);
    }
  }

  @Override
  public void dispose() {
    disposeIfNeeded(retryPolicyTemplate, LOGGER);
  }

  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(retryPolicyTemplate, injector);
    managementStrategyFactory = new ConnectionManagementStrategyFactory(defaultPoolingProfile, deploymentLifecycleState,
                                                                        xaConnectionManagementStrategyFactory);
  }

  @Override
  public void start() throws ZernovaException {
    startIfNeeded(retryPolicyTemplate);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PoolingProfile getDefaultPoolingProfile() {
    return defaultPoolingProfile;
  }

}
