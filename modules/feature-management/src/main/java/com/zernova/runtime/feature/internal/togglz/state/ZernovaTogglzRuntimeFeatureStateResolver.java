/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.togglz.state;

import static org.togglz.core.context.FeatureContext.getFeatureManager;

import com.zernova.runtime.feature.internal.togglz.scope.ZernovaTogglzFeatureScope;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ZernovaTogglzFeatureStateResolver} for the runtime scope.
 *
 * @since 4.5.0
 */
public class ZernovaTogglzRuntimeFeatureStateResolver implements ZernovaTogglzFeatureStateResolver {

  private final Map<Feature, FeatureState> featureStates = new ConcurrentHashMap<>();
  private final ZernovaTogglzFeatureStateRepository stateRepository;

  public ZernovaTogglzRuntimeFeatureStateResolver(ZernovaTogglzFeatureStateRepository stateRepository) {
    this.stateRepository = stateRepository;
  }

  @Override
  public FeatureState getFeatureState(Feature feature, ZernovaTogglzFeatureScope scope) {
    return featureStates.computeIfAbsent(feature,
                                         ft -> new ZernovaTogglzFeatureState(getFeatureManager().getMetaData(ft)
                                             .getDefaultFeatureState(), stateRepository, scope));
  }

  @Override
  public FeatureState setFeatureState(ZernovaTogglzFeatureScope scope, FeatureState featureState) {
    return featureStates
        .computeIfAbsent(featureState.getFeature(), feature -> new ZernovaTogglzFeatureState(feature, stateRepository, scope))
        .setEnabled(featureState.isEnabled());
  }

  @Override
  public void removeFeatureFeature(ZernovaTogglzFeatureState muleFeatureState) {
    this.featureStates.remove(muleFeatureState.getFeature());
  }
}
