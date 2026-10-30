/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.config;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;


/**
 * A component responsible for providing instances which are realizations of a given {@link ConfigurationModel}.
 * <p/>
 * Instances are provided through the {@link #get(Event)} method. When that method is invoked, it's up to each implementation to
 * return a brand new instance or one which has already been returned before.
 *
 * @since 1.0
 */
@NoImplement
public interface ConfigurationProvider extends Component {

  /**
   * Returns a {@link ConfigurationInstance}
   * <p/>
   * This method may return an instance already returned in the past or a brand new one.
   *
   * @param event the event which processing requires the instance
   * @return a {@link ConfigurationInstance}
   */
  ConfigurationInstance get(Event event);

  /**
   * @return the {@link ExtensionModel} which owns the {@link #getConfigurationModel()}
   */
  ExtensionModel getExtensionModel();

  /**
   * @return the {@link ConfigurationModel} for the instances returned by {@link #get(Event)}
   */
  ConfigurationModel getConfigurationModel();

  /**
   * The name under which this provider has been registered
   *
   * @return this provider's name
   */
  String getName();

  /**
   * @return Whether the returned config is dynamic or static
   */
  boolean isDynamic();
}
