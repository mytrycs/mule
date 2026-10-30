/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.togglz.scope;

import com.zernova.runtime.feature.internal.togglz.scope.type.ZernovaTogglzFeatureScopeType;

import java.util.Objects;

import static com.zernova.runtime.feature.internal.togglz.scope.type.ZernovaTogglzFeatureScopeType.ARTIFACT_SCOPE_TYPE;

/**
 * A {@link ZernovaTogglzFeatureScope} that represents the scope of artifact. When a feature is enabled/disabled using this scope, it
 * will change the status for the corresponding artifact, that is.
 *
 * @since 4.5.0
 */
public class ZernovaTogglzArtifactFeatureScope implements ZernovaTogglzFeatureScope {

  private String artifactId;

  public ZernovaTogglzArtifactFeatureScope(String artifactId) {
    this.artifactId = artifactId;
  }

  public String getArtifactId() {
    return this.artifactId;
  }

  @Override
  public ZernovaTogglzFeatureScopeType getScopeType() {
    return ARTIFACT_SCOPE_TYPE;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ZernovaTogglzArtifactFeatureScope that = (ZernovaTogglzArtifactFeatureScope) o;
    return Objects.equals(artifactId, that.artifactId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(artifactId);
  }
}
