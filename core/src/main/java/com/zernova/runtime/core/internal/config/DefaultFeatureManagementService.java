/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.config;

import static com.zernova.runtime.feature.internal.togglz.config.ZernovaTogglzFeatureFlaggingUtils.getFeature;
import static com.zernova.runtime.feature.internal.togglz.config.ZernovaTogglzFeatureFlaggingUtils.setFeatureState;
import static com.zernova.runtime.feature.internal.togglz.config.ZernovaTogglzFeatureFlaggingUtils.withFeatureUser;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.feature.api.management.FeatureFlaggingManagementService;
import com.zernova.runtime.feature.internal.togglz.user.ZernovaTogglzArtifactFeatureUser;

import jakarta.inject.Inject;

import org.togglz.core.repository.FeatureState;

/**
 * The default implementation for a service to enable/disable runtime features.
 *
 * @since 4.5.0
 */
public class DefaultFeatureManagementService implements FeatureFlaggingManagementService {

  @Inject
  private ZernovaContext muleContext;

  @Override
  public void disableFeatureFor(String featureName) {
    setStatus(featureName, false);
  }

  @Override
  public void enableFeatureFor(String featureName) {
    setStatus(featureName, true);
  }

  private void setStatus(String featureName, boolean status) {
    withFeatureUser(new ZernovaTogglzArtifactFeatureUser(muleContext.getConfiguration().getId()),
                    () -> setFeatureState(new FeatureState(getFeature(featureName), status)));
  }
}
