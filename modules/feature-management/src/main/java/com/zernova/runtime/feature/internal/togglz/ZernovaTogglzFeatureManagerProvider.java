/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.togglz;

import com.zernova.runtime.feature.internal.togglz.config.ZernovaHotSwitchProfilingFeatures;
import com.zernova.runtime.feature.internal.togglz.provider.DefaultZernovaTogglzFeatureProvider;
import com.zernova.runtime.feature.internal.togglz.state.ZernovaTogglzFeatureStateRepository;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.spi.FeatureManagerProvider;
import org.togglz.core.user.thread.ThreadLocalUserProvider;

import static com.zernova.runtime.feature.internal.togglz.activation.ZernovaTogglzActivationStrategyProvider.getDefaultActivationStrategyProvider;

/**
 * a {@link FeatureManagerProvider} for the Mule Runtime. This is retrieved through SPI.
 *
 * @since 4.5.0
 */
public class ZernovaTogglzFeatureManagerProvider implements FeatureManagerProvider {

  public static final int PRIORITY = 30;
  public static DefaultZernovaTogglzFeatureProvider FEATURE_PROVIDER =
      new DefaultZernovaTogglzFeatureProvider(ZernovaHotSwitchProfilingFeatures.class);

  private static FeatureManager FEATURE_MANAGER = new FeatureManagerBuilder()
      .featureProvider(FEATURE_PROVIDER)
      .stateRepository(new ZernovaTogglzFeatureStateRepository(FEATURE_PROVIDER))
      .userProvider(new ThreadLocalUserProvider())
      .activationStrategyProvider(getDefaultActivationStrategyProvider())
      .build();


  @Override
  public FeatureManager getFeatureManager() {
    return FEATURE_MANAGER;
  }

  @Override
  public int priority() {
    // Not used. By default.
    return PRIORITY;
  }
}
