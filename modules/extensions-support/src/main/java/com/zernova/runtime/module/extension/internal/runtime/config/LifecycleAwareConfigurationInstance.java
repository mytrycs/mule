/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.config;

import static java.lang.String.format;
import static com.zernova.runtime.api.util.Preconditions.checkState;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.internal.config.ConfigurationInstanceNotification.CONFIGURATION_STOPPED;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.api.time.TimeSupplier;
import com.zernova.runtime.core.api.Injector;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.connector.ConnectionManager;
import com.zernova.runtime.core.internal.config.ConfigurationInstanceNotification;
import com.zernova.runtime.core.internal.connection.ConnectionManagerAdapter;
import com.zernova.runtime.core.internal.connection.ConnectivityTester;
import com.zernova.runtime.core.internal.connection.ConnectivityTesterFactory;
import com.zernova.runtime.core.internal.time.LocalTimeSupplier;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationState;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationStats;

import java.util.Optional;

import jakarta.inject.Inject;

import org.slf4j.Logger;

/**
 * Implementation of {@link ConfigurationInstance} which propagates dependency injection and lifecycle phases into the contained
 * configuration {@link #value} and {@link #connectionProvider} (if present).
 * <p>
 * In the case of the {@link #connectionProvider} being present, then it also binds the {@link #value} to the
 * {@link ConnectionProvider} by the means of {@link ConnectionManager#bind(Object, ConnectionProvider)} when the
 * {@link #initialise()} phase is executed. That bound will be broken on the {@link #stop()} phase by using
 * {@link ConnectionManager#unbind(Object)}
 *
 * @since 4.0
 */
public final class LifecycleAwareConfigurationInstance implements ConfigurationInstance, Lifecycle {

  private static final Logger LOGGER = getLogger(LifecycleAwareConfigurationInstance.class);

  private final String name;
  private final ConfigurationModel model;
  private final Object value;
  private final ConfigurationState configurationState;
  private final Optional<ConnectionProvider> connectionProvider;

  private ConfigurationStats configurationStats;

  private TimeSupplier timeSupplier;
  private NotificationDispatcher notificationFirer;
  private ConnectionManagerAdapter connectionManager;
  private ConnectivityTesterFactory connectivityTesterFactory;
  private Injector injector;

  private volatile boolean initialized = false;
  private volatile boolean started = false;

  private ConnectivityTester connectivityTester;

  /**
   * Creates a new instance
   *
   * @param name               this configuration's name
   * @param model              the {@link ConfigurationModel} for this instance
   * @param value              the actual configuration instance
   * @param connectionProvider an {@link Optional} containing the {@link ConnectionProvider} to use
   */
  public LifecycleAwareConfigurationInstance(String name,
                                             ConfigurationModel model,
                                             Object value,
                                             ConfigurationState configurationState,
                                             Optional<ConnectionProvider> connectionProvider) {
    this.name = name;
    this.model = model;
    this.value = value;
    this.configurationState = configurationState;
    this.connectionProvider = connectionProvider;
  }

  @Override
  public synchronized void initialise() throws InitialisationException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(format("Initializing LifecycleAwareConfigurationInstance '%s'", getName()));
    }

    if (!initialized) {
      initialized = true;
      try {
        initStats();
        doInitialise();
      } catch (Exception e) {
        LOGGER.error(format("Error initializing LifecycleAwareConfigurationInstance '%s'", getName()), e);

        if (e instanceof InitialisationException) {
          throw (InitialisationException) e;
        } else {
          throw new InitialisationException(e, this);
        }
      }
    }
  }

  @Override
  public synchronized void start() throws ZernovaException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(format("Starting LifecycleAwareConfigurationInstance '%s'", getName()));
    }

    if (!started) {
      started = true;
      if (connectionProvider.isPresent()) {
        startIfNeeded(connectionProvider);
        if (!connectionManager.hasBinding(value)) {
          connectionManager.bind(value, connectionProvider.get());
        }

        connectivityTester = connectivityTesterFactory.create(getName());
        connectivityTester.testConnectivity(connectionProvider.get(), this);
      }
      startIfNeeded(value);
    }
  }

  /**
   * Propagates this lifecycle phase into the the {@link #value}. Also triggers a {@link ConfigurationInstanceNotification} that
   * is being stopped.
   *
   * @throws ZernovaException if an exception is found
   */
  @Override
  public synchronized void stop() throws ZernovaException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(format("Stopping LifecycleAwareConfigurationInstance '%s'", getName()));
    }

    if (started) {
      started = false;
      try {
        stopIfNeeded(value);
        if (connectionProvider.isPresent()) {
          if (connectivityTester == null) {
            unbindConnection();
          } else {
            connectivityTester.withTestConnectivityLock(this::unbindConnection);
          }
        }
        connectivityTester = null;
      } finally {
        notificationFirer.dispatch(new ConfigurationInstanceNotification(this, CONFIGURATION_STOPPED));
      }
    }
  }

  protected void unbindConnection() throws ZernovaException {
    connectionManager.unbind(value);
    stopIfNeeded(connectionProvider);
  }

  /**
   * Propagates this lifecycle phase into the the {@link #value}.
   */
  @Override
  public synchronized void dispose() {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(format("Disposing LifecycleAwareConfigurationInstance '%s'", getName()));
    }

    if (initialized) {
      initialized = false;
      disposeIfNeeded(value, LOGGER);
      disposeIfNeeded(connectionProvider, LOGGER);
      configurationStats = null;
    }
  }

  private void doInitialise() throws InitialisationException {
    if (connectionProvider.isPresent()) {
      initialiseIfNeeded(connectionProvider, injector);
      initialiseIfNeeded(connectionManager);
      connectionManager.bind(value, connectionProvider.get());
    }

    initialiseIfNeeded(value, injector);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<ConnectionProvider> getConnectionProvider() {
    return connectionProvider;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConfigurationModel getModel() {
    return model;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getValue() {
    return value;
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalStateException if invoked before {@link #initialise()}
   */
  @Override
  public ConfigurationStats getStatistics() {
    checkState(configurationStats != null, "can't get statistics before initialise() is invoked");
    return configurationStats;
  }

  @Override
  public ConfigurationState getState() {
    return configurationState;
  }

  private void initStats() {
    if (timeSupplier == null) {
      timeSupplier = new LocalTimeSupplier();
    }

    configurationStats = new DefaultMutableConfigurationStats(timeSupplier);
  }

  @Inject
  public void setTimeSupplier(TimeSupplier timeSupplier) {
    this.timeSupplier = timeSupplier;
  }

  @Inject
  public void setNotificationFirer(NotificationDispatcher notificationFirer) {
    this.notificationFirer = notificationFirer;
  }

  @Inject
  public void setConnectionManager(ConnectionManagerAdapter connectionManager) {
    this.connectionManager = connectionManager;
  }

  @Inject
  public void setConnectivityTesterFactory(ConnectivityTesterFactory connectivityTesterFactory) {
    this.connectivityTesterFactory = connectivityTesterFactory;
  }

  public void setInjector(Injector injector) {
    this.injector = injector;
  }

  @Inject
  public void setMuleContext(ZernovaContext muleContext) {
    setInjector(muleContext.getInjector());
  }
}
