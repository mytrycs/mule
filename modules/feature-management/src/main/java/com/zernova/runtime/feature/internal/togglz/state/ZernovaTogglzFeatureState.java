/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.togglz.state;

import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.feature.internal.togglz.scope.ZernovaTogglzFeatureScope;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

/**
 * This represents a Mule Togglz Feature State.
 *
 * @since 4.5.0
 */
public class ZernovaTogglzFeatureState extends FeatureState implements Disposable {

  private final ZernovaTogglzFeatureStateRepository stateRepository;
  private final ZernovaTogglzFeatureScope scope;

  /**
   * A Mule Togglz Feature Wrapper for a {@link FeatureState}
   *
   * @param feature         the feature
   * @param stateRepository the feature repository
   * @param scope           the {@link ZernovaTogglzFeatureScope} for the state
   */
  public ZernovaTogglzFeatureState(Feature feature,
                                ZernovaTogglzFeatureStateRepository stateRepository,
                                ZernovaTogglzFeatureScope scope) {
    super(feature);
    this.scope = scope;
    this.stateRepository = stateRepository;
  }

  public ZernovaTogglzFeatureState(FeatureState defaultFeatureState,
                                ZernovaTogglzFeatureStateRepository stateRepository,
                                ZernovaTogglzFeatureScope scope) {
    super(defaultFeatureState.getFeature(), defaultFeatureState.isEnabled());
    this.scope = scope;
    this.stateRepository = stateRepository;
  }

  @Override
  public void dispose() {
    stateRepository.removeFeatureState(this);
  }

  public ZernovaTogglzFeatureScope getScope() {
    return scope;
  }
}
