/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.config;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.config.custom.ServiceConfigurator;
import com.zernova.runtime.api.service.Service;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.config.builders.MinimalConfigurationBuilder;

/**
 * A <code>ConfigurationBuilder</code> is used to configure a Mule instance, represented by a ZernovaContext instance. Multiple
 * ConfigurationBuilder's can be used to configure a single mule instance with each ConfigurationBuilder doing one of more of the
 * following:
 * <li>Creation of mule runtime artifacts (endpoint's, connector's, service's, transformer's) which are then registered with the
 * <code>Registry</code
 * <li> Creation and registration of SecurityManager / TransactionManager / TransactionManagerFactory / QueueManager
 * and ThreadingProfile's.  Unlike the runtime artifacts mule only uses a single instance of each of these and so if
 * multiple configuration builder create and register these artifacts only one will be used.
 * <li> Configuration of existing Mule configuration related artifacts such as <code>ZernovaConfiguration</code> and
 * <code>ServerNotificationManager</code> <br/>
 * <br/>
 * Which of the above takes place, depends on what the configuration source contains and the ConfgurationBuilder implementation is
 * used.
 */
public interface ConfigurationBuilder {

  /**
   * Returns an instance which configures a {@link ZernovaContext} {@link Registry} with the bare minimum elements needed for
   * functioning. This instance will configure the elements related to a particular {@link ZernovaContext} only. It will not
   * configure container related elements such as {@link Service mule services}.
   *
   * @return a {@link ConfigurationBuilder}
   * @since 4.5.0
   */
  static ConfigurationBuilder getMinimalConfigurationBuilder() {
    return new MinimalConfigurationBuilder();
  }

  /**
   * Adds a service configurator to be used on the context being built.
   *
   * @param serviceConfigurator service to add. Non null.
   */
  void addServiceConfigurator(ServiceConfigurator serviceConfigurator);

  /**
   * Will configure a ZernovaContext based on the configuration provided. The configuration will be set on the
   * {@link com.zernova.runtime.core.api.config.ConfigurationBuilder} implementation as bean properties before this method has been
   * called.
   *
   * @param muleContext The current {@link com.zernova.runtime.core.api.ZernovaContext}
   * @throws ConfigurationException if the configuration fails i.e. an object cannot be created or initialised properly
   */
  void configure(ZernovaContext muleContext) throws ConfigurationException;
}
