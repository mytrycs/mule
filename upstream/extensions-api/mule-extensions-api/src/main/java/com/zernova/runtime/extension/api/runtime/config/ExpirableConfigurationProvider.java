/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.config;

import com.zernova.api.annotation.NoImplement;

import java.util.List;

/**
 * A specialization of the {@link ConfigurationProvider} interface which adds the concept of expiration. The runtime will query
 * the registered {@link ConfigurationProvider configuration providers} which implement this interface when it tries to locate
 * expired configurations which need disposal
 *
 * @since 1.0
 */
@NoImplement
public interface ExpirableConfigurationProvider extends ConfigurationProvider {

  /**
   * @return a {@link List} which items are the expired {@link ConfigurationInstance}
   */
  List<ConfigurationInstance> getExpired();
}
