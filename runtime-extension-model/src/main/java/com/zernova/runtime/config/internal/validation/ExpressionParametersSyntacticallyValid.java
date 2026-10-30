/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.ast.api.util.ComponentAstPredicatesFactory.currentElemement;
import static com.zernova.runtime.extension.api.util.ExtensionModelUtils.getGroupAndParametersPairs;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.zernova.runtime.api.el.ExpressionLanguage;
import com.zernova.runtime.api.el.validation.Severity;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.ComponentParameterAst;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ExpressionParametersSyntacticallyValid extends AbstractExpressionSyntacticallyValid {

  public ExpressionParametersSyntacticallyValid(ExpressionLanguage expressionLanguage, Supplier<Level> level, Severity severity) {
    super(expressionLanguage, level, severity);
  }

  @Override
  public String getName() {
    return "Expression are syntactically valid";
  }

  @Override
  public String getDescription() {
    return "Expression are syntactically valid";
  }

  @Override
  public Predicate<List<ComponentAst>> applicable() {
    return currentElemement(c -> c.getModel(ParameterizedModel.class)
        .map(pmzd -> pmzd.getParameterGroupModels().stream()
            .anyMatch(pmg -> pmg.getParameterModels().stream()
                .anyMatch(pm -> {
                  ComponentParameterAst param = c.getParameter(pmg.getName(), pm.getName());
                  return param != null && param.getValue().isLeft();
                })))
        .orElse(false));
  }

  @Override
  public List<ValidationResultItem> validateMany(ComponentAst component, ArtifactAst artifact) {
    return component.getModel(ParameterizedModel.class)
        .map(pmz -> getGroupAndParametersPairs(pmz)
            .map(gnp -> component.getParameter(gnp.getFirst().getName(), gnp.getSecond().getName()))
            .filter(param -> param != null && param.getValue().isLeft())
            .flatMap(param -> validateExpression(component, param, param.getValue().getLeft()))
            .collect(toList()))
        .orElse(emptyList());
  }

}
