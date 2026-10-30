/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.internal.artifact;

import static com.zernova.runtime.ast.api.util.ZernovaAstUtils.emptyArtifact;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.registry.DefaultRegistry;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;

/**
 * Simple implementation of {@link ArtifactContext}
 *
 * @since 4.2
 */
public class ImmutableArtifactContext implements ArtifactContext {

  private final ZernovaContext muleContext;
  private final Registry registry;

  public ImmutableArtifactContext(ZernovaContext muleContext) {
    this.muleContext = muleContext;
    registry = new DefaultRegistry(muleContext);
  }

  @Override
  public ZernovaContext getMuleContext() {
    return muleContext;
  }

  @Override
  public Registry getRegistry() {
    return registry;
  }

  @Override
  public ArtifactAst getArtifactAst() {
    return emptyArtifact();
  }
}
