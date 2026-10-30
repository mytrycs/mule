/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.togglz.scope.type;

import com.zernova.runtime.feature.internal.togglz.scope.ZernovaTogglzFeatureScope;

/**
 * Mule Togglz Feature Scope Types.
 *
 * @see ZernovaTogglzFeatureScope
 *
 * @since 4.5.0
 */
public enum ZernovaTogglzFeatureScopeType {

  /**
   * Represents a type of {@link ZernovaTogglzFeatureScope} that corresponds to an artifact.
   */
  ARTIFACT_SCOPE_TYPE,

  /**
   * Represents a type of {@link ZernovaTogglzFeatureScope} that corresponds to the whole runtime.
   */
  RUNTIME_SCOPE_TYPE;
}
