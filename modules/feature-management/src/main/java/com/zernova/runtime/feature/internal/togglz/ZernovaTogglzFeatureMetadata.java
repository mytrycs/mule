/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.togglz;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import com.zernova.runtime.feature.internal.togglz.activation.strategies.ZernovaTogglzActivatedIfEnabledActivationStrategy;
import org.togglz.core.Feature;
import org.togglz.core.metadata.FeatureGroup;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.repository.FeatureState;

import java.util.Map;
import java.util.Set;


/**
 * A {@link FeatureMetaData} used for the features defined by the runtime.
 *
 * @since 4.5.0
 */
public class ZernovaTogglzFeatureMetadata implements FeatureMetaData {

  private final FeatureState defaultFeatureState;

  public ZernovaTogglzFeatureMetadata(Feature newFeature) {
    this(newFeature, false);
  }

  public ZernovaTogglzFeatureMetadata(Feature newFeature, boolean enabled) {
    this.defaultFeatureState = new FeatureState(newFeature, enabled);
    this.defaultFeatureState.setStrategyId(ZernovaTogglzActivatedIfEnabledActivationStrategy.ID);
  }

  @Override
  public String getLabel() {
    return "Mule Runtime defined Feature";
  }

  @Override
  public FeatureState getDefaultFeatureState() {
    return defaultFeatureState.copy();
  }

  @Override
  public Set<FeatureGroup> getGroups() {
    return emptySet();
  }

  @Override
  public Map<String, String> getAttributes() {
    return emptyMap();
  }
}
