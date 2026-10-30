/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.api.deployable;

import com.zernova.runtime.api.deployment.meta.ZernovaApplicationModel;
import com.zernova.runtime.api.deployment.meta.ZernovaDeployableModel;
import com.zernova.runtime.api.deployment.meta.ZernovaDomainModel;
import com.zernova.runtime.api.deployment.persistence.ZernovaApplicationModelJsonSerializer;
import com.zernova.runtime.api.deployment.persistence.ZernovaDomainModelJsonSerializer;
import com.zernova.runtime.module.artifact.activation.internal.deployable.JsonDeserializingArtifactModelResolver;

import java.io.File;

/**
 * Provides a way to obtain a {@link ZernovaDeployableModel} serialized in a JSON file.
 *
 * @param <M> the concrete type of model (application or model) to resolve.
 *
 * @since 4.5
 */
public interface ArtifactModelResolver<M extends ZernovaDeployableModel> {

  /**
   * @return a default resolver for applications.
   */
  static ArtifactModelResolver<ZernovaApplicationModel> applicationModelResolver() {
    return new JsonDeserializingArtifactModelResolver<>(new ZernovaApplicationModelJsonSerializer());
  }

  /**
   * @return a default resolver for domains.
   */
  static ArtifactModelResolver<ZernovaDomainModel> domainModelResolver() {
    return new JsonDeserializingArtifactModelResolver<>(new ZernovaDomainModelJsonSerializer());
  }

  /**
   * Loads and deserializes a {@code zernova-artifact.json} file.
   *
   * @param artifactLocation the folder containing the {@code zernova-artifact.json} file to deserialize.
   * @return the deserialized {@link ZernovaDeployableModel}.
   */
  M resolve(File artifactLocation);
}
