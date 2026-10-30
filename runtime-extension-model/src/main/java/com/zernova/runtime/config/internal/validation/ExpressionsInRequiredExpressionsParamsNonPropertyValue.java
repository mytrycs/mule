/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.api.component.ComponentIdentifier.builder;
import static com.zernova.runtime.api.meta.ExpressionSupport.REQUIRED;
import static com.zernova.runtime.ast.api.util.ComponentAstPredicatesFactory.currentElemement;
import static com.zernova.runtime.ast.api.util.ZernovaAstUtils.hasPropertyPlaceholder;
import static com.zernova.runtime.ast.api.validation.Validation.Level.WARN;
import static com.zernova.runtime.ast.api.validation.ValidationResultItem.create;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.CORE_PREFIX;
import static com.zernova.runtime.extension.api.util.ExtensionModelUtils.getGroupAndParametersPairs;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.validation.Validation;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;

import java.util.List;
import java.util.function.Predicate;

/**
 * Expressions are fixed for parameters that require expressions.
 *
 * @since 4.5
 */
public class ExpressionsInRequiredExpressionsParamsNonPropertyValue implements Validation {

  private static final String CONFIGURATION_NAME = "configuration";

  protected static final ComponentIdentifier CONFIGURATION_IDENTIFIER =
      builder().namespace(CORE_PREFIX).name(CONFIGURATION_NAME).build();

  @Override
  public String getName() {
    return "Expression in expressionsRequired params are fixed";
  }

  @Override
  public String getDescription() {
    return "Expressions are fixed for parameters that require expressions.";
  }

  @Override
  public Level getLevel() {
    return WARN;
  }

  @Override
  public Predicate<List<ComponentAst>> applicable() {
    return currentElemement(component -> component.getModel(ParameterizedModel.class)
        .map(pmz -> pmz.getAllParameterModels())
        .orElse(emptyList())
        .stream()
        .filter(pm -> REQUIRED.equals(pm.getExpressionSupport()))
        .findAny()
        .isPresent());
  }

  @Override
  public List<ValidationResultItem> validateMany(ComponentAst component, ArtifactAst artifact) {
    return component.getModel(ParameterizedModel.class)
        .map(pmz -> getGroupAndParametersPairs(pmz)
            .filter(gnp -> REQUIRED.equals(gnp.getSecond().getExpressionSupport()))
            .map(gnp -> component.getParameter(gnp.getFirst().getName(), gnp.getSecond().getName()))
            .collect(toList()))
        .orElse(emptyList())
        .stream()
        .filter(param -> hasPropertyPlaceholder(param.getRawValue()))
        .map(param -> create(component, param, this,
                             format("Parameter '%s' has value '%s' which is resolved with a property and may cause the artifact to have different behavior on different environments.",
                                    param.getModel().getName(), param.getRawValue())))
        .collect(toList());
  }
}
