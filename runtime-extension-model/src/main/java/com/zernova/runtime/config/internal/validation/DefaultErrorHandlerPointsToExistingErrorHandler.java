/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.api.component.ComponentIdentifier.builder;
import static com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel.DEFAULT_GROUP_NAME;
import static com.zernova.runtime.ast.api.util.ComponentAstPredicatesFactory.currentElemement;
import static com.zernova.runtime.ast.api.util.ComponentAstPredicatesFactory.equalsComponentId;
import static com.zernova.runtime.ast.api.util.ComponentAstPredicatesFactory.equalsIdentifier;
import static com.zernova.runtime.ast.api.util.ZernovaAstUtils.hasPropertyPlaceholder;
import static com.zernova.runtime.ast.api.validation.Validation.Level.ERROR;
import static com.zernova.runtime.ast.api.validation.ValidationResultItem.create;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.CORE_PREFIX;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.ComponentParameterAst;
import com.zernova.runtime.ast.api.validation.Validation;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 'defaultErrorHandler-ref' points to existing error-handler.
 */
public class DefaultErrorHandlerPointsToExistingErrorHandler implements Validation {

  private static final String CONFIGURATION_ELEMENT = "configuration";

  public static final ComponentIdentifier CONFIGURATION_IDENTIFIER =
      builder().namespace(CORE_PREFIX).name(CONFIGURATION_ELEMENT).build();

  private final boolean ignoreParamsWithProperties;

  public DefaultErrorHandlerPointsToExistingErrorHandler(boolean ignoreParamsWithProperties) {
    this.ignoreParamsWithProperties = ignoreParamsWithProperties;
  }

  @Override
  public String getName() {
    return "'defaultErrorHandler-ref' points to existing error-handler";
  }

  @Override
  public String getDescription() {
    return "'defaultErrorHandler-ref' points to existing error-handler.";
  }

  @Override
  public Level getLevel() {
    return ERROR;
  }

  @Override
  public Predicate<List<ComponentAst>> applicable() {
    return currentElemement(equalsIdentifier(CONFIGURATION_IDENTIFIER))
        .and(currentElemement(component -> {
          ComponentParameterAst defaultErrorHandlerNameParameter =
              component.getParameter(DEFAULT_GROUP_NAME, "defaultErrorHandler-ref");
          if (ignoreParamsWithProperties && hasPropertyPlaceholder(defaultErrorHandlerNameParameter.getRawValue())) {
            return false;
          }

          return defaultErrorHandlerNameParameter.getValue().isRight();
        }));
  }

  @Override
  public Optional<ValidationResultItem> validate(ComponentAst component, ArtifactAst artifact) {
    final ComponentParameterAst param = component.getParameter(DEFAULT_GROUP_NAME, "defaultErrorHandler-ref");
    return param.getValue()
        .reduce(l -> empty(),
                nameAttribute -> {
                  if (artifact.topLevelComponentsStream()
                      .noneMatch(equalsComponentId((String) nameAttribute))) {
                    return of(create(component, param, this,
                                     "No global error handler defined with name '" + nameAttribute + "'."));
                  } else {
                    return empty();
                  }
                });
  }

}
