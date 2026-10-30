/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.togglz.user;

import com.zernova.runtime.feature.internal.togglz.scope.ZernovaTogglzArtifactFeatureScope;
import org.togglz.core.user.FeatureUser;

/**
 * A {@link ZernovaFeatureUser} that corresponds to an artifact.
 *
 * @since 4.5.0
 */
public class ZernovaTogglzArtifactFeatureUser implements ZernovaFeatureUser, FeatureUser {

  private final String name;
  private final ZernovaTogglzArtifactFeatureScope scope;

  public ZernovaTogglzArtifactFeatureUser(String artifactId) {
    this.name = artifactId;
    this.scope = new ZernovaTogglzArtifactFeatureScope(artifactId);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isFeatureAdmin() {
    return false;
  }

  @Override
  public Object getAttribute(String name) {
    if (name.equals(FEATURE_SCOPE_ATTRIBUTE_KEY)) {
      return scope;
    }

    return null;
  }
}
