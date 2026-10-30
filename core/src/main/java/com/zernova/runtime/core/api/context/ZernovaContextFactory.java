/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.context;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.context.notification.ZernovaContextListener;

import java.util.List;

/**
 * A ZernovaContextFactory is used to create instances of {@link ZernovaContext}. The instances of {@link ZernovaContext} returned by this
 * factory are initialised but not started.
 */
@NoImplement
public interface ZernovaContextFactory {

  /**
   * Creates a new ZernovaContext using the given configurationBuilder
   *
   * @param configurationBuilders
   * @throws InitialisationException
   * @throws ConfigurationException
   */
  ZernovaContext createMuleContext(ConfigurationBuilder... configurationBuilders)
      throws InitialisationException, ConfigurationException;

  /**
   * Creates a new ZernovaContext using the {@link ZernovaContextBuilder} provided and configures it with the list of configuration
   * builder and c onfigures it with configurationBuilder
   *
   * @param configurationBuilder
   * @throws InitialisationException
   * @throws ConfigurationException
   */
  ZernovaContext createMuleContext(ConfigurationBuilder configurationBuilder, ZernovaContextBuilder muleContextBuilder)
      throws InitialisationException, ConfigurationException;

  /**
   * Creates a new ZernovaContext using the {@link ZernovaContextBuilder} provided and configures it with the list of configuration
   * builders. Configuration builders will be invoked in the same or as provided in the List.
   *
   * @throws InitialisationException
   * @throws ConfigurationException
   */
  ZernovaContext createMuleContext(List<ConfigurationBuilder> configurationBuilders, ZernovaContextBuilder muleContextBuilder)
      throws InitialisationException, ConfigurationException;

  /**
   * Adds a listener to be notified of each event related to the creation of a new {@link ZernovaContext}
   *
   * @param listener listener to add. Must be non null.
   */
  void addListener(ZernovaContextListener listener);

  /**
   * Removes a listener from the factory.
   *
   *
   * @param listener the listener to remove. After this call, the listener won't receive any notifications from this factory.
   * @return true if the listener was registered in the factory, false otherwise
   */
  boolean removeListener(ZernovaContextListener listener);
}
