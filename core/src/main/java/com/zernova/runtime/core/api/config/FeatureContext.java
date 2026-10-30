/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.config;

import static java.util.Optional.ofNullable;

import com.zernova.runtime.api.meta.ZernovaVersion;

import java.util.Optional;

/**
 * Decoupled {@link com.zernova.runtime.core.api.ZernovaContext} metadata, used to evaluate {@link com.zernova.runtime.api.config.Feature}
 * flags.
 *
 * @since 4.4.0
 */
public class FeatureContext {

  private final String artifactName;
  private ZernovaVersion artifactMinMuleVersion;

  public FeatureContext(ZernovaVersion artifactMinMuleVersion, String artifactName) {
    this.artifactName = artifactName;
    // Feature flags must evaluate against non suffixed versions
    if (artifactMinMuleVersion != null) {
      this.artifactMinMuleVersion = artifactMinMuleVersion.withoutSuffixes();
    }
  }

  public Optional<ZernovaVersion> getArtifactMinMuleVersion() {
    return ofNullable(artifactMinMuleVersion);
  }

  public String getArtifactName() {
    return ofNullable(artifactName).orElse("");
  }
}
