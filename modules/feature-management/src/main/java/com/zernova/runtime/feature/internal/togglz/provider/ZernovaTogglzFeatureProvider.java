/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.togglz.provider;

import com.zernova.runtime.api.profiling.type.ProfilingEventType;
import com.zernova.runtime.feature.internal.togglz.ZernovaTogglzProfilingFeature;
import com.zernova.runtime.feature.internal.togglz.ZernovaTogglzRuntimeFeature;
import org.togglz.core.Feature;
import org.togglz.core.spi.FeatureProvider;

/**
 * Implementation of {@link FeatureProvider} for the Mule Runtime.
 *
 * @since 4.5.0
 */
public interface ZernovaTogglzFeatureProvider extends FeatureProvider {

  /**
   * Gets a {@link Feature} associated with a {@link com.zernova.runtime.api.config.Feature}
   *
   * @param feature the runtime {@link com.zernova.runtime.api.config.Feature}
   * @return the registered Togglz {@link Feature}
   */
  Feature getRuntimeTogglzFeature(com.zernova.runtime.api.config.Feature feature);

  /**
   * Gets a Togglz feature associated with a {@link com.zernova.runtime.api.config.Feature}
   *
   * @param feature the runtime {@link com.zernova.runtime.api.config.Feature}
   * @return the registered {@link Feature}. If the feature is already registered it returns the corresponding togglz feature.
   */
  ZernovaTogglzRuntimeFeature getOrRegisterRuntimeTogglzFeatureFrom(com.zernova.runtime.api.config.Feature feature);

  /**
   * Registers a Togglz {@link Feature} associated to a {@link ProfilingEventType} and a
   * {@link com.zernova.runtime.api.profiling.ProfilingDataConsumer}
   *
   * @param profilingEventType the {@link ProfilingEventType}.
   * @param consumerName       the id associated to a {@link com.zernova.runtime.api.profiling.ProfilingDataConsumer}
   * @return the registered {@link Feature}
   */
  ZernovaTogglzProfilingFeature getOrRegisterProfilingTogglzFeatureFrom(ProfilingEventType<?> profilingEventType,
                                                                     String consumerName);

  /**
   * returns a {@link Feature} by name
   *
   * @param featureName the name for the feature
   * @return the coresponding {@link Feature}
   */
  Feature getFeature(String featureName);

}
