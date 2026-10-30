/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.togglz.config;

import com.zernova.runtime.feature.internal.togglz.state.ZernovaTogglzFeatureState;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import java.util.Map;

/**
 * Represents the features associated to an artifact.
 *
 * @since 4.5.0
 */
public class ZernovaTogglzManagedArtifactFeatures {

  private final String artifactId;
  private final Map<Feature, FeatureState> features;

  public ZernovaTogglzManagedArtifactFeatures(String artifactId,
                                           Map<Feature, FeatureState> features) {
    this.artifactId = artifactId;
    this.features = features;
  }

  public boolean containsKey(Object key) {
    return features.containsKey(key);
  }

  /**
   * @return the artifact id for the feature
   */
  public String getArtifactId() {
    return artifactId;
  }

  /**
   * disposes the feature states.
   */
  public void dispose() {
    for (FeatureState featureState : features.values()) {
      if (featureState instanceof ZernovaTogglzFeatureState) {
        ((ZernovaTogglzFeatureState) featureState).dispose();
      }
    }
  }

  /**
   * @param togglzFeature the {@link Feature}
   * @return the corresponding FeatureS
   */
  public FeatureState get(Feature togglzFeature) {
    return features.get(togglzFeature);
  }
}
