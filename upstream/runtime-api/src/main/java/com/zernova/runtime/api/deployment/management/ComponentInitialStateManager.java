/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.deployment.management;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.util.ZernovaSystemProperties;

/**
 * Manages the initial state of components within a mule artifact (application, domain, policy, etc).
 */
@NoImplement
public interface ComponentInitialStateManager {

  /**
   * This is a configuration property that can be set at deployment time to disable the scheduler message sources to be started
   * when deploying an application.
   *
   * @deprecated Please use {@link com.zernova.runtime.api.util.ZernovaSystemProperties#DISABLE_SCHEDULER_SOURCES_PROPERTY} instead.
   */
  @Deprecated
  String DISABLE_SCHEDULER_SOURCES_PROPERTY = ZernovaSystemProperties.DISABLE_SCHEDULER_SOURCES_PROPERTY;

  /**
   * Service ID to use to customize the {@link ComponentInitialStateManager} instance for the application through the use of the
   * {@link com.zernova.runtime.api.config.custom.CustomizationService}.
   */
  String SERVICE_ID = "_muleComponentInitialStateManager";

  /**
   * Manages the initial state of sources. This method will be called before starting the source for the first time to know if
   * it's initial state must be started or not.
   *
   * @param component the source component to be started
   * @return true if the source must be started, false otherise.
   */
  default boolean mustStartMessageSource(Component component) {
    return true;
  }

}
