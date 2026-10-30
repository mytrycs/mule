/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.model.dsl.config;

import static com.zernova.runtime.api.source.SchedulerMessageSource.SCHEDULER_MESSAGE_SOURCE_IDENTIFIER;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.runtime.api.deployment.management.ComponentInitialStateManager;
import com.zernova.runtime.api.util.ZernovaSystemProperties;

import jakarta.inject.Inject;

/**
 * Default {@link ComponentInitialStateManager} that always signals initialization of components.
 */
public class DefaultComponentInitialStateManager implements ComponentInitialStateManager {

  @Inject
  private ConfigurationProperties configurationProperties;

  @Override
  public boolean mustStartMessageSource(Component component) {
    if (!configurationProperties.resolveProperty(ZernovaSystemProperties.DISABLE_SCHEDULER_SOURCES_PROPERTY).isPresent()) {
      return true;
    }
    if (component.getIdentifier().equals(SCHEDULER_MESSAGE_SOURCE_IDENTIFIER)) {
      return !configurationProperties.resolveBooleanProperty(ZernovaSystemProperties.DISABLE_SCHEDULER_SOURCES_PROPERTY)
          .orElse(false);
    } else {
      return true;
    }
  }

}
