/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel.ERROR_MAPPINGS;
import static com.zernova.runtime.ast.api.util.ComponentAstPredicatesFactory.currentElemement;
import static com.zernova.runtime.ast.api.validation.Validation.Level.ERROR;
import static com.zernova.runtime.ast.api.validation.ValidationResultItem.create;
import static com.zernova.runtime.extension.api.ExtensionConstants.ERROR_MAPPINGS_PARAMETER_NAME;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;
import com.zernova.runtime.extension.api.error.ErrorMapping;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Referenced error types do exist in the context of the artifact
 */
public class ErrorMappingSourceTypeReferencesExist extends AbstractErrorTypesValidation {

  public ErrorMappingSourceTypeReferencesExist(Optional<FeatureFlaggingService> featureFlaggingService,
                                               boolean ignoreParamsWithProperties) {
    super(featureFlaggingService, ignoreParamsWithProperties);
  }

  @Override
  public String getName() {
    return "Error Type references exist";
  }

  @Override
  public String getDescription() {
    return "Referenced error types do exist in the context of the artifact.";
  }

  @Override
  public Level getLevel() {
    return ERROR;
  }

  @Override
  public Predicate<List<ComponentAst>> applicable() {
    return currentElemement(((Predicate<ComponentAst>) this::errorMappingPresent)
        .and(comp -> isIgnoreParamsWithProperties()
            ? errorMappingSourceNotPropertyDependant(comp)
            : true));
  }

  @Override
  public Optional<ValidationResultItem> validate(ComponentAst component, ArtifactAst artifact) {
    for (ErrorMapping errorMapping : getErrorMappings(component)) {
      final ComponentIdentifier errorTypeId = parseErrorType(errorMapping.getSource());

      final Optional<ErrorType> errorType = artifact.getErrorTypeRepository().lookupErrorType(errorTypeId);
      if (!errorType.isPresent()) {
        return of(create(component, component.getParameter(ERROR_MAPPINGS, ERROR_MAPPINGS_PARAMETER_NAME), this,
                         format("Could not find error '%s'.", errorMapping.getSource())));
      }
    }

    return empty();
  }

}
