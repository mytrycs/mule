/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.togglz.scope;

import com.zernova.runtime.feature.internal.togglz.scope.type.ZernovaTogglzFeatureScopeType;

/**
 * The scope for the state of a {@link org.togglz.core.Feature}. In each scope, the
 * {@link org.togglz.core.repository.FeatureState} can vary.
 *
 * @see com.zernova.runtime.feature.internal.togglz.state.ZernovaTogglzFeatureStateRepository
 *
 * @since 4.5.0
 */
public interface ZernovaTogglzFeatureScope {

  /**
   * The scope type.
   *
   * @see ZernovaTogglzFeatureScopeType
   *
   * @return the scope type
   */
  ZernovaTogglzFeatureScopeType getScopeType();

}
