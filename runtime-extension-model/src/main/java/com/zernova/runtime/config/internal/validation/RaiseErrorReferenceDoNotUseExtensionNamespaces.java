/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.ast.api.util.ComponentAstPredicatesFactory.currentElemement;
import static com.zernova.runtime.ast.api.util.ComponentAstPredicatesFactory.equalsIdentifier;
import static com.zernova.runtime.ast.api.validation.ValidationResultItem.create;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.ComponentParameterAst;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Referenced error types do not use extension namespaces
 */
public class RaiseErrorReferenceDoNotUseExtensionNamespaces extends AbstractErrorTypesValidation {

  public RaiseErrorReferenceDoNotUseExtensionNamespaces(Optional<FeatureFlaggingService> featureFlaggingService) {
    super(featureFlaggingService, false);
  }

  @Override
  public String getName() {
    return "Error Type references don't use extension namespaces";
  }

  @Override
  public String getDescription() {
    return "Referenced error types do not use extension namespaces.";
  }

  @Override
  public Predicate<List<ComponentAst>> applicable() {
    return currentElemement(equalsIdentifier(RAISE_ERROR_IDENTIFIER)
        // there is already another validation for the presence of this param
        .and(this::isErrorTypePresent));
  }

  @Override
  public Optional<ValidationResultItem> validate(ComponentAst component, ArtifactAst artifact) {
    final ComponentParameterAst errorTypeParam = getErrorTypeParam(component);
    final String errorTypeString = errorTypeParam.getResolvedRawValue();
    final Set<String> alreadyUsedErrorNamespaces = getAlreadyUsedErrorNamespaces(artifact);
    final ComponentIdentifier errorTypeId = parseErrorType(errorTypeString);

    String namespace = errorTypeId.getNamespace();
    if (alreadyUsedErrorNamespaces.contains(namespace) && !isAllowedBorrowedNamespace(namespace)) {
      return of(create(component, errorTypeParam, this,
                       format("Cannot use error type '%s': namespace already exists.", errorTypeString)));
    }

    return empty();
  }

}
