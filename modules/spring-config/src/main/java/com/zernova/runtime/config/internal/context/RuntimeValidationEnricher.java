/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.context;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.ast.api.validation.ValidationsProvider;
import com.zernova.runtime.ast.graph.api.ArtifactAstDependencyGraphProvider;
import com.zernova.runtime.config.internal.validation.ast.ArtifactAstGraphDependencyProviderAware;
import com.zernova.runtime.core.api.ZernovaContext;

import java.util.function.Consumer;

/**
 * The runtime enricher for the {@link ValidationsProvider}'s.
 */
public class RuntimeValidationEnricher implements Consumer<ValidationsProvider> {

  private final ArtifactAstDependencyGraphProvider artifactAstGraphDependencyProvider;
  private final ZernovaContext muleContext;

  public RuntimeValidationEnricher(ArtifactAstDependencyGraphProvider artifactAstGraphDependencyProvider,
                                   ZernovaContext muleContext) {
    this.artifactAstGraphDependencyProvider = artifactAstGraphDependencyProvider;
    this.muleContext = muleContext;
  }

  @Override
  public void accept(ValidationsProvider validationsProvider) {
    injectDependencies(validationsProvider);
    setArtifactAstDependencyGraphProviderIfPossible(validationsProvider);
  }

  private void setArtifactAstDependencyGraphProviderIfPossible(ValidationsProvider validationsProvider) {
    if (validationsProvider instanceof ArtifactAstGraphDependencyProviderAware) {
      ((ArtifactAstGraphDependencyProviderAware) validationsProvider)
          .setArtifactAstDependencyGraphProvider(artifactAstGraphDependencyProvider);
    }
  }

  private void injectDependencies(ValidationsProvider validationsProvider) {
    try {
      muleContext.getInjector().inject(validationsProvider);
    } catch (ZernovaException e) {
      throw new ZernovaRuntimeException(e);
    }
  }
}
