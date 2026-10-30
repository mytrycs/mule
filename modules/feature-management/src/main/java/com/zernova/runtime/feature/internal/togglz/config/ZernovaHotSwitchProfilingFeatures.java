/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.togglz.config;

import org.togglz.core.Feature;
import org.togglz.core.activation.SystemPropertyActivationStrategy;
import org.togglz.core.annotation.ActivationParameter;
import org.togglz.core.annotation.DefaultActivationStrategy;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.repository.FeatureState;

import static java.lang.Thread.currentThread;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.ENABLE_PROFILING_SERVICE_PROPERTY;
import static org.togglz.core.context.FeatureContext.getFeatureManager;

/**
 * The definition of Profiling/Troubleshooting {@link Feature}'s.
 *
 * @since 4.5.0
 */
public enum ZernovaHotSwitchProfilingFeatures implements Feature {

  @Label("Profiling service to enable disable profiling")
  @EnabledByDefault
  @DefaultActivationStrategy(id = SystemPropertyActivationStrategy.ID,
      parameters = {
          @ActivationParameter(name = SystemPropertyActivationStrategy.PARAM_PROPERTY_NAME,
              value = ENABLE_PROFILING_SERVICE_PROPERTY)
      })
  PROFILING_SERVICE_FEATURE;

  public boolean isActive() {
    ClassLoader cl = currentThread().getContextClassLoader();
    try {
      currentThread().setContextClassLoader(ZernovaHotSwitchProfilingFeatures.class.getClassLoader());
      return getFeatureManager().isActive(this);
    } finally {
      currentThread().setContextClassLoader(cl);
    }
  }

  public FeatureState getFeatureState() {
    ClassLoader cl = currentThread().getContextClassLoader();
    try {
      currentThread().setContextClassLoader(ZernovaHotSwitchProfilingFeatures.class.getClassLoader());
      return getFeatureManager().getFeatureState(this);
    } finally {
      currentThread().setContextClassLoader(cl);
    }
  }
}
