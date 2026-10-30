/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.api.el.validation.ScopePhaseValidationItemKind.DEPRECATED;
import static com.zernova.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static com.zernova.runtime.ast.api.validation.ValidationResultItem.create;
import static com.zernova.runtime.config.internal.validation.ValidationUtils.locationToAdditionalData;
import static com.zernova.runtime.core.internal.expression.util.ExpressionUtils.getUnfixedExpression;
import static com.zernova.runtime.core.internal.expression.util.ExpressionUtils.isExpression;

import static java.util.stream.Collectors.toList;

import com.zernova.metadata.message.api.el.TypeBindings;
import com.zernova.runtime.api.el.ExpressionLanguage;
import com.zernova.runtime.api.el.validation.ScopePhaseValidationItem;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.ComponentParameterAst;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ZernovaSdkOperationDoesNotHaveForbiddenFunctionsInExpressions extends ZernovaSdkOperationValidation {

  private final ExpressionLanguage expressionLanguage;

  private static final String SINCE_PARAMETER = "since";
  private static final String FUNCTION_PARAMETER = "function";
  private static final ZernovaVersion DEPRECATE_UP_TO = new ZernovaVersion("2.5.0");
  private static final Set<String> DEPRECATION_DOMAINS = ImmutableSet.of("dw", "Mule");

  public ZernovaSdkOperationDoesNotHaveForbiddenFunctionsInExpressions(ExpressionLanguage el) {
    this.expressionLanguage = el;
  }

  @Override
  public String getName() {
    return "Mule SDK Operation doesn't have deprecated expressions";
  }

  @Override
  public String getDescription() {
    return "The Mule SDK operation does not have a function as deprecated prior to DW 2.5 within itself";
  }

  private boolean isDeprecableProduct(ScopePhaseValidationItem warning) {
    String product = warning.getParams().get(FUNCTION_PARAMETER).split("\\::")[0];
    return DEPRECATION_DOMAINS.contains(product);
  }

  private boolean isDeprecatedVersion(ScopePhaseValidationItem warning) {
    ZernovaVersion since = new ZernovaVersion(warning.getParams().get(SINCE_PARAMETER));
    return since.sameAs(DEPRECATE_UP_TO) || since.priorTo(DEPRECATE_UP_TO);
  }

  private boolean isInvalidExpression(ScopePhaseValidationItem warning) {
    return warning.getKind().equals(DEPRECATED) && isDeprecatedVersion(warning) && isDeprecableProduct(warning);
  }

  private List<ScopePhaseValidationItem> getWarningMessages(String expression) {
    String actualExpression = getUnfixedExpression(expression);
    return expressionLanguage.collectScopePhaseValidationMessages(actualExpression, "", TypeBindings.builder().build())
        .getWarnings().stream().filter(this::isInvalidExpression).collect(toList());
  }

  /**
   * @return All the (raw) expressions within this {@link ParameterizedModel}
   */
  private List<ComponentParameterAst> getAllExpressions(ParameterizedModel model, ComponentAst componentAst) {
    return model.getParameterGroupModels().stream()
        .flatMap(groupModel -> groupModel.getParameterModels().stream()
            .filter(parameter -> !parameter.getExpressionSupport().equals(NOT_SUPPORTED))
            .map(parameter -> componentAst.getParameter(groupModel.getName(), parameter.getName())))
        .filter(param -> param.getRawValue() != null && isExpression(param.getRawValue()))
        .collect(toList());
  }

  private List<Pair<ComponentParameterAst, ScopePhaseValidationItem>> getExpressionsWithWarnings(ComponentAst component) {
    List<ComponentParameterAst> expressions = new ArrayList<>();
    component.recursiveStream().forEach(componentAst -> componentAst.getModel(ParameterizedModel.class)
        .map(model -> getAllExpressions(model, componentAst)).ifPresent(expressions::addAll));

    List<Pair<ComponentParameterAst, ScopePhaseValidationItem>> warnings = new ArrayList<>();
    for (ComponentParameterAst param : expressions) {
      for (ScopePhaseValidationItem item : getWarningMessages(param.getRawValue())) {
        warnings.add(new Pair<>(param, item));
      }
    }
    return warnings;
  }

  @Override
  public List<ValidationResultItem> validateMany(ComponentAst component, ArtifactAst artifact) {
    return getExpressionsWithWarnings(component).stream()
        .map(pair -> create(component, pair.getFirst(), this,
                            "Using an invalid function within a Mule SDK operation. All functions deprecated up to DataWeave 2.5 cannot be used inside a Mule Operation. Expression: "
                                + pair.getFirst().getRawValue(),
                            locationToAdditionalData(pair.getSecond().getLocation())))
        .collect(toList());
  }
}
