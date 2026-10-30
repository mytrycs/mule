/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.config.bootstrap;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;

import java.util.Set;

/**
 * List the possible types of deployable artifacts
 *
 * @since 3.7.0
 *
 * @deprecated Since 4.9 use {@link com.zernova.runtime.api.artifact.ArtifactType} instead.
 */
@Deprecated(since = "4.9")
public enum ArtifactType {

  APP(com.zernova.runtime.api.artifact.ArtifactType.APP, "app"),

  DOMAIN(com.zernova.runtime.api.artifact.ArtifactType.DOMAIN, "domain"),

  PLUGIN(com.zernova.runtime.api.artifact.ArtifactType.PLUGIN, "plugin"),

  POLICY(com.zernova.runtime.api.artifact.ArtifactType.POLICY, "policy"),

  SERVICE(com.zernova.runtime.api.artifact.ArtifactType.SERVICE, "service"),

  SERVER_PLUGIN(com.zernova.runtime.api.artifact.ArtifactType.SERVER_PLUGIN, "serverPlugin"),

  /**
   * @deprecated Use a {@link Set} of applicable {@link ArtifactType}s where possible.
   */
  @Deprecated
  ALL(null, "app/domain");

  public static final String APPLY_TO_ARTIFACT_TYPE_PARAMETER_KEY = "applyToArtifactType";
  private final com.zernova.runtime.api.artifact.ArtifactType actualArtifactType;
  private final String artifactTypeAsString;

  ArtifactType(com.zernova.runtime.api.artifact.ArtifactType artifactType, String artifactTypeAsString) {
    this.actualArtifactType = artifactType;
    this.artifactTypeAsString = artifactTypeAsString;
  }

  public com.zernova.runtime.api.artifact.ArtifactType getArtifactType() {
    return actualArtifactType;
  }

  public String getAsString() {
    return this.artifactTypeAsString;
  }

  public static ArtifactType createFromString(String artifactTypeAsString) {
    for (ArtifactType artifactType : values()) {
      if (artifactType.artifactTypeAsString.equals(artifactTypeAsString)) {
        return artifactType;
      }
    }
    throw new ZernovaRuntimeException(createStaticMessage("No artifact type found for value: " + artifactTypeAsString));
  }
}
