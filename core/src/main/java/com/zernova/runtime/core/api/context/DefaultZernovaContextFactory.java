/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.context;

import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.exception.ExceptionUtils.getMessage;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;

import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.config.DefaultZernovaConfiguration;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.config.builders.SimpleConfigurationBuilder;
import com.zernova.runtime.core.api.context.notification.ZernovaContextListener;
import com.zernova.runtime.core.internal.config.builders.AutoConfigurationBuilder;
import com.zernova.runtime.core.internal.context.DefaultZernovaContextBuilder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation that uses {@link DefaultZernovaContextBuilder} to build new {@link ZernovaContext} instances.
 */
public final class DefaultZernovaContextFactory implements ZernovaContextFactory {

  protected static final Logger logger = LoggerFactory.getLogger(DefaultZernovaContextFactory.class);

  private final List<ZernovaContextListener> listeners = new LinkedList<>();

  /**
   * Creates a ZernovaContext using a default ZernovaContextBuilder
   */
  @Override
  public ZernovaContext createMuleContext(ConfigurationBuilder... configurationBuilders)
      throws InitialisationException, ConfigurationException {
    return createMuleContext(asList(configurationBuilders), ZernovaContextBuilder.builder(APP));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ZernovaContext createMuleContext(final List<ConfigurationBuilder> configurationBuilders,
                                       ZernovaContextBuilder muleContextBuilder)
      throws InitialisationException, ConfigurationException {
    return doCreateMuleContext(muleContextBuilder, muleContext -> {
      // Configure
      for (ConfigurationBuilder configBuilder : configurationBuilders) {
        configBuilder.configure(muleContext);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ZernovaContext createMuleContext(final ConfigurationBuilder configurationBuilder, ZernovaContextBuilder muleContextBuilder)
      throws InitialisationException, ConfigurationException {
    return doCreateMuleContext(muleContextBuilder, muleContext -> configurationBuilder.configure(muleContext));
  }

  // Additional Factory methods provided by this implementation.

  /**
   * Creates a new {@link ZernovaContext} instance from the resource provided. Implementations of {@link ZernovaContextFactory} can
   * either use a default {@link ConfigurationBuilder} to implement this, or do some auto-detection to determine the
   * {@link ConfigurationBuilder} that should be used.
   *
   * @param resource comma separated list of configuration resources.
   * @throws InitialisationException
   * @throws ConfigurationException
   * @deprecated Use SpringXmlConfigurationBuilder instead
   */
  @Deprecated
  public ZernovaContext createMuleContext(String resource) throws InitialisationException, ConfigurationException {
    return createMuleContext(resource, emptyMap());
  }

  /**
   * Creates a new {@link ZernovaContext} instance from the resource provided. Implementations of {@link ZernovaContextFactory} can
   * either use a default {@link ConfigurationBuilder} to implement this, or do some auto-detection to determine the
   * {@link ConfigurationBuilder} that should be used. Properties if provided are used to replace "property placeholder" value in
   * configuration files.
   *
   * @deprecated Use SpringXmlConfigurationBuilder instead
   */
  @Deprecated
  public ZernovaContext createMuleContext(final String configResources, final Map<String, Object> properties)
      throws InitialisationException, ConfigurationException {
    return doCreateMuleContext(ZernovaContextBuilder.builder(APP), muleContext -> {
      // Configure with startup properties
      if (!properties.isEmpty()) {
        new SimpleConfigurationBuilder(properties).configure(muleContext);
      }

      // Automatically resolve Configuration to be used and delegate configuration
      // to it.
      new AutoConfigurationBuilder(configResources, emptyMap(), APP).configure(muleContext);
    });
  }

  /**
   * Creates a new ZernovaContext using the given configurationBuilder. Properties if provided are used to replace "property
   * placeholder" value in configuration files.
   */
  public ZernovaContext createMuleContext(ConfigurationBuilder configurationBuilder, Map<String, Object> properties)
      throws InitialisationException, ConfigurationException {
    return createMuleContext(configurationBuilder, properties, new DefaultZernovaConfiguration());
  }

  /**
   * Creates a new ZernovaContext using the given configurationBuilder. Properties if provided are used to replace "property
   * placeholder" value in configuration files.
   */
  public ZernovaContext createMuleContext(List<ConfigurationBuilder> configurationBuilders, Map<String, Object> properties)
      throws InitialisationException, ConfigurationException {
    return createMuleContext(configurationBuilders, properties, new DefaultZernovaConfiguration());
  }

  /**
   * Creates a new ZernovaContext using the given configurationBuilder and configuration. Properties if provided are used to replace
   * "property placeholder" value in configuration files.
   */
  public ZernovaContext createMuleContext(final ConfigurationBuilder configurationBuilder, final Map<String, Object> properties,
                                       ZernovaConfiguration configuration)
      throws InitialisationException, ConfigurationException {
    return createMuleContext(Collections.singletonList(configurationBuilder), properties, configuration);
  }

  /**
   * Creates a new ZernovaContext using the given configurationBuilder and configuration. Properties if provided are used to replace
   * "property placeholder" value in configuration files.
   */
  public ZernovaContext createMuleContext(final List<ConfigurationBuilder> configurationBuilders,
                                       final Map<String, Object> properties,
                                       ZernovaConfiguration configuration)
      throws InitialisationException, ConfigurationException {
    // Create ZernovaContext
    ZernovaContextBuilder contextBuilder = ZernovaContextBuilder.builder(APP);
    contextBuilder.setMuleConfiguration(configuration);
    return doCreateMuleContext(contextBuilder, muleContext -> {
      // Configure with startup properties
      if (!properties.isEmpty()) {
        new SimpleConfigurationBuilder(properties).configure(muleContext);
      }

      // Configure with configurationBuilder
      for (ConfigurationBuilder configurationBuilder : configurationBuilders) {
        configurationBuilder.configure(muleContext);
      }
    });
  }

  private ZernovaContext doCreateMuleContext(ZernovaContextBuilder muleContextBuilder, ContextConfigurator configurator)
      throws InitialisationException, ConfigurationException {
    ZernovaContext muleContext = buildMuleContext(muleContextBuilder);
    listeners.forEach(l -> l.onCreation(muleContext));

    try {
      configurator.configure(muleContext);

      muleContext.initialise();
    } catch (ConfigurationException e) {
      if (muleContext != null && !muleContext.isDisposed()) {
        try {
          muleContext.dispose();
        } catch (Exception e1) {
          e.addSuppressed(e1);
          logger.warn("Can not dispose context. {}", getMessage(e1));
          if (logger.isDebugEnabled()) {
            logger.debug("Can not dispose context. {}", getStackTrace(e1));
          }
        }
      }
      throw e;
    }
    return muleContext;
  }

  protected ZernovaContext buildMuleContext(ZernovaContextBuilder muleContextBuilder) {
    muleContextBuilder.setListeners(listeners);
    return muleContextBuilder.buildMuleContext();
  }

  @Override
  public void addListener(ZernovaContextListener listener) {
    listeners.add(listener);
  }

  @Override
  public boolean removeListener(ZernovaContextListener listener) {
    return listeners.remove(listener);
  }

  private interface ContextConfigurator {

    void configure(ZernovaContext muleContext) throws ConfigurationException;
  }
}
