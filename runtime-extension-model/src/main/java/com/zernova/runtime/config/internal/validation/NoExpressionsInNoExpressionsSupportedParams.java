/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.api.component.ComponentIdentifier.builder;
import static com.zernova.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static com.zernova.runtime.ast.api.util.ComponentAstPredicatesFactory.currentElemement;
import static com.zernova.runtime.ast.api.util.ComponentAstPredicatesFactory.equalsIdentifier;
import static com.zernova.runtime.ast.api.validation.Validation.Level.WARN;
import static com.zernova.runtime.ast.api.validation.ValidationResultItem.create;
import static com.zernova.runtime.core.internal.expression.util.ExpressionUtils.isExpression;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.CORE_PREFIX;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.validation.Validation;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;
import com.zernova.runtime.extension.api.declaration.type.annotation.LiteralTypeAnnotation;

import java.util.List;
import java.util.function.Predicate;

/**
 * No expressions are provided for parameters that do not support expressions.
 */
public class NoExpressionsInNoExpressionsSupportedParams implements Validation {

  private static final String FLOW_REF_ELEMENT = "flow-ref";

  private static final ComponentIdentifier FLOW_REF_IDENTIFIER =
      builder().namespace(CORE_PREFIX).name(FLOW_REF_ELEMENT).build();

  @Override
  public String getName() {
    return "No expressions in no expressionsSupported params";
  }

  @Override
  public String getDescription() {
    return "No expressions are provided for parameters that do not support expressions.";
  }

  @Override
  public Level getLevel() {
    return WARN;
  }

  @Override
  public Predicate<List<ComponentAst>> applicable() {
    return currentElemement(component -> component.getModel(ParameterizedModel.class).isPresent())
        // According to the extension model, flow-ref cannot be dynamic,
        // But this check is needed to avoid breaking on legacy cases that use dynamic flow-refs.
        .and(currentElemement(equalsIdentifier(FLOW_REF_IDENTIFIER).negate()));
  }

  @Override
  public List<ValidationResultItem> validateMany(ComponentAst component, ArtifactAst artifact) {
    return component.getParameters()
        .stream()
        .filter(param -> !param.getModel().isComponentId())
        .filter(param -> param.getValueOrResolutionError().isRight()
            && param.getResolvedRawValue() instanceof String)
        .filter(param -> NOT_SUPPORTED.equals(param.getModel().getExpressionSupport())
            && !param.getModel().getType().getAnnotation(LiteralTypeAnnotation.class).isPresent())
        .filter(param -> isExpression(param.getResolvedRawValue()))
        .map(param -> create(component, param, this,
                             format("An expression value was given for parameter '%s' but it doesn't support expressions",
                                    param.getModel().getName())))
        .collect(toList());
  }

}
