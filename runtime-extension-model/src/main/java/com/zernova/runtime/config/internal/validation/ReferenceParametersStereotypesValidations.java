/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.ast.api.validation.Validation.Level.WARN;
import static com.zernova.runtime.config.internal.validation.DefaultErrorHandlerPointsToExistingErrorHandler.CONFIGURATION_IDENTIFIER;
import static com.zernova.runtime.config.internal.validation.FlowRefPointsToExistingFlow.FLOW_REF_IDENTIFIER;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.APP_CONFIG;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.CONFIG;

import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.graph.api.ArtifactAstDependencyGraphProvider;
import com.zernova.runtime.ast.graph.api.ComponentAstDependency;

import java.util.function.Predicate;

public class ReferenceParametersStereotypesValidations extends AbstractReferenceParametersStereotypesValidations {

  public ReferenceParametersStereotypesValidations(ArtifactAstDependencyGraphProvider artifactAstDependencyGraphProvider) {
    super(artifactAstDependencyGraphProvider);
  }

  @Override
  public String getName() {
    return "Reference parameters stereotypes";
  }

  @Override
  public String getDescription() {
    return "Reference parameters point to declarations of the appropriate stereotype.";
  }

  @Override
  public Level getLevel() {
    // Keep backwards compatibility with some scenarios that are expected to deploy and fail at runtime rather than deployment
    return WARN;
  }

  @Override
  protected Predicate<? super ComponentAstDependency> filterArtifact(ArtifactAst artifact) {
    return missing ->
    // flow-ref is already validated by FlowRefPointsToExistingFlow
    !missing.getComponent().getIdentifier().equals(FLOW_REF_IDENTIFIER)
        // configuration.defaultErrorHandler-ref is already validated by DefaultErrorHandlerPointsToExistingErrorHandler
        // configuration.defaultObjectSerializer-ref is already validated by DefaultObjectSerializerPointsToExistingSerializer
        && !missing.getComponent().getIdentifier().equals(CONFIGURATION_IDENTIFIER)
        && missing.getAllowedStereotypes().stream()
            .noneMatch(st -> st.isAssignableTo(CONFIG)
                || st.isAssignableTo(APP_CONFIG));
  }

}
