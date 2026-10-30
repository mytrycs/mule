/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.ast.graph.api.ArtifactAstDependencyGraphFactory.generateFor;

import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.graph.api.ArtifactAstDependencyGraph;
import com.zernova.runtime.ast.graph.api.ArtifactAstDependencyGraphProvider;

/**
 * A default implementation for the {@link ArtifactAstDependencyGraphProvider}.
 */
public class DefaultArtifactAstDependencyGraphProvider implements ArtifactAstDependencyGraphProvider {

  @Override
  public ArtifactAstDependencyGraph get(ArtifactAst fullArtifactAst) {
    return generateFor(fullArtifactAst);
  }
}
