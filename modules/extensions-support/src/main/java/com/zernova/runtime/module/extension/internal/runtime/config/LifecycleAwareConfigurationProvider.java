/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.config;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getClassLoader;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.api.exception.DefaultZernovaException;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.lifecycle.DefaultLifecycleManager;
import com.zernova.runtime.core.internal.lifecycle.SimpleLifecycleManager;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;

/**
 * Base class for implementations of {@link ConfigurationProvider} which keep track of the {@link ConfigurationInstance} they
 * generate and propagate lifecycle and IoC into them.
 * <p/>
 * It also implements the other common concerns of every {@link ConfigurationProvider}, leaving implementations with the need to
 * &quot;just&quot; implement {@link #get(Event)}
 *
 * @since 4.0
 */
public abstract class LifecycleAwareConfigurationProvider extends AbstractComponent
    implements ConfigurationProvider, Lifecycle {

  private static final Logger LOGGER = getLogger(LifecycleAwareConfigurationProvider.class);

  private final String name;
  private final ExtensionModel extensionModel;
  private final ConfigurationModel configurationModel;
  private final List<ConfigurationInstance> configurationInstances = new LinkedList<>();
  private final ClassLoader extensionClassLoader;
  protected final SimpleLifecycleManager lifecycleManager;
  protected final ZernovaContext muleContext;

  public LifecycleAwareConfigurationProvider(String name, ExtensionModel extensionModel, ConfigurationModel configurationModel,
                                             ZernovaContext muleContext) {
    this.name = requireNonNull(name);
    this.extensionModel = requireNonNull(extensionModel);
    this.configurationModel = requireNonNull(configurationModel);
    this.muleContext = requireNonNull(muleContext);
    extensionClassLoader = getClassLoader(extensionModel);
    lifecycleManager = new DefaultLifecycleManager<>(format("%s-%s", getClass().getName(), getName()), this);
  }

  /**
   * Performs dependency injection into all the currently provided configurations, and when needed, fires the
   * {@link Initialisable#initialise()} phase on them
   *
   * @throws InitialisationException if an exception is found
   */
  @Override
  public void initialise() throws InitialisationException {
    withContextClassLoader(extensionClassLoader, () -> {
      lifecycleManager.fireInitialisePhase((phaseName, object) -> {
        for (ConfigurationInstance configurationInstance : configurationInstances) {
          initialiseIfNeeded(configurationInstance, true, muleContext);
        }
        doInitialise();
      });
      return null;
    }, InitialisationException.class, e -> {
      throw new InitialisationException(e, this);
    });
  }

  /**
   * Template method so subclasses can do initialisation.
   */
  protected void doInitialise() {}

  /**
   * When needed, fires the {@link Startable#start()} phase on the currently provided configurations
   *
   * @throws ZernovaException if an exception is found
   */
  @Override
  public void start() throws ZernovaException {
    withContextClassLoader(extensionClassLoader, () -> {
      lifecycleManager.fireStartPhase((phaseName, object) -> {
        for (ConfigurationInstance configurationInstance : configurationInstances) {
          startConfig(configurationInstance);
        }
      });
      return null;
    }, ZernovaException.class, e -> {
      throw new DefaultZernovaException(e);
    });
  }

  /**
   * When needed, fires the {@link Stoppable#stop()} phase on the currently provided configurations
   *
   * @throws ZernovaException if an exception is found
   */
  @Override
  public void stop() throws ZernovaException {
    withContextClassLoader(extensionClassLoader, () -> {
      lifecycleManager.fireStopPhase((phaseName, object) -> {
        for (ConfigurationInstance configurationInstance : configurationInstances) {
          stopIfNeeded(configurationInstance);
        }
      });
      return null;
    }, ZernovaException.class, e -> {
      throw new DefaultZernovaException(e);
    });
  }

  /**
   * When needed, fires the {@link Disposable#dispose()} phase on the currently provided configurations
   */
  @Override
  public void dispose() {
    try {
      withContextClassLoader(extensionClassLoader, () -> {
        lifecycleManager.fireDisposePhase((phaseName, object) -> {
          for (ConfigurationInstance configurationInstance : configurationInstances) {
            disposeIfNeeded(configurationInstance, LOGGER);
          }
        });
        return null;
      });
    } catch (Exception e) {
      LOGGER.error("Could not dispose configuration provider of name " + getName(), e);
    }
  }

  /**
   * Implementations are to invoke this method everytime they create a new {@link ConfigurationInstance} so that they're kept
   * track of and the lifecycle can be propagated
   *
   * @param configuration a newly created {@link ConfigurationInstance}
   */
  protected void registerConfiguration(ConfigurationInstance configuration) {
    configurationInstances.add(configuration);
  }

  /**
   * Implementations are to invoke this method every time they want to unregister a current registered
   * {@link ConfigurationInstance} so that they're no longer kept track of and the lifecycle is not propagated.
   *
   * @param configuration an already registered {@link ConfigurationInstance}
   * @return whether a {@link ConfigurationInstance} was unregistered or not.
   *
   */
  protected boolean unRegisterConfiguration(ConfigurationInstance configuration) {
    return configurationInstances.remove(configuration);
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
  public ConfigurationModel getConfigurationModel() {
    return configurationModel;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExtensionModel getExtensionModel() {
    return extensionModel;
  }

  protected void startConfig(ConfigurationInstance config) throws ZernovaException {
    startIfNeeded(config);
  }

  protected ClassLoader getExtensionClassLoader() {
    return extensionClassLoader;
  }
}
