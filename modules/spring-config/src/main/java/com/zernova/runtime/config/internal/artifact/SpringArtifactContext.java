/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.artifact;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.config.internal.context.ZernovaArtifactContext;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.runtime.module.artifact.api.Artifact;

/**
 * An artifact context contains all the information related to an {@link Artifact} that contains configuration.
 * <p/>
 * This object holds the created {@link ZernovaContext} and the {@link ZernovaArtifactContext} which holds the information related to
 * the application configuration and resources.
 *
 * @since 4.0
 */
public class SpringArtifactContext implements ArtifactContext {

  private final ZernovaArtifactContext muleArtifactContext;

  /**
   * Creates an {@link ArtifactContext}.
   *
   * @param muleArtifactContext the artifact context.
   */
  public SpringArtifactContext(ZernovaArtifactContext muleArtifactContext) {
    this.muleArtifactContext = muleArtifactContext;
  }

  /**
   * @return the artifact {@link ZernovaContext}
   */
  @Override
  public ZernovaContext getMuleContext() {
    return this.muleArtifactContext.getMuleContext();
  }

  @Override
  public Registry getRegistry() {
    return this.muleArtifactContext.getRegistry();
  }

  @Override
  public ArtifactAst getArtifactAst() {
    return this.muleArtifactContext.getApplicationModel();
  }
}
