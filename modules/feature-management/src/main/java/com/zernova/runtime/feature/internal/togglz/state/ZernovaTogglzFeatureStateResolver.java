/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.togglz.state;

import com.zernova.runtime.feature.internal.togglz.scope.ZernovaTogglzFeatureScope;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

/**
 * Resolves the feature state according to a type of scope
 *
 * @since 4.5.0
 */
public interface ZernovaTogglzFeatureStateResolver {

  /**
   * @param feature the {@link Feature}
   * @param scope   the {@link ZernovaTogglzFeatureScope}
   * @return the {@link FeatureState} for the corresponding feature
   */
  FeatureState getFeatureState(Feature feature, ZernovaTogglzFeatureScope scope);

  /**
   * @param scope        the {@link ZernovaTogglzFeatureScope}
   * @param featureState the new {@link FeatureState}
   * @return the new {@link FeatureState} for the corresponding feature
   */
  FeatureState setFeatureState(ZernovaTogglzFeatureScope scope, FeatureState featureState);

  /**
   * Removes the {@link ZernovaTogglzFeatureState}
   *
   * @param muleFeatureState the {@link ZernovaTogglzFeatureState}
   */
  void removeFeatureFeature(ZernovaTogglzFeatureState muleFeatureState);
}
