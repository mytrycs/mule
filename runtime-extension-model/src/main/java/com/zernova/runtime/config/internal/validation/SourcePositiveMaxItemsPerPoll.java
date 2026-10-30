/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel.DEFAULT_GROUP_NAME;
import static com.zernova.runtime.ast.api.util.ComponentAstPredicatesFactory.currentElemement;
import static com.zernova.runtime.ast.api.validation.Validation.Level.ERROR;
import static com.zernova.runtime.ast.api.validation.ValidationResultItem.create;
import static com.zernova.runtime.extension.api.ExtensionConstants.POLLING_SOURCE_LIMIT_PARAMETER_NAME;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.zernova.runtime.api.functional.Either;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.ComponentParameterAst;
import com.zernova.runtime.ast.api.validation.Validation;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SourcePositiveMaxItemsPerPoll implements Validation {

  @Override
  public String getName() {
    return "Source 'maxItemsPerPoll' is positive";
  }

  @Override
  public String getDescription() {
    return "Source 'maxItemsPerPoll' is positive";
  }

  @Override
  public Level getLevel() {
    return ERROR;
  }

  @Override
  public Predicate<List<ComponentAst>> applicable() {
    Predicate<ComponentAst> source = component -> component.getModel(SourceModel.class).isPresent();
    Predicate<ComponentAst> hasParam = component -> {
      ComponentParameterAst parameter = component.getParameter(DEFAULT_GROUP_NAME, POLLING_SOURCE_LIMIT_PARAMETER_NAME);
      return parameter != null && !parameter.getValue().equals(Either.empty());
    };

    return currentElemement(source.and(hasParam));
  }

  @Override
  public Optional<ValidationResultItem> validate(ComponentAst component, ArtifactAst artifact) {
    ComponentParameterAst parameter = component.getParameter(DEFAULT_GROUP_NAME, POLLING_SOURCE_LIMIT_PARAMETER_NAME);
    int maxItemsPerPoll = (int) parameter.getValue().getRight();
    if (maxItemsPerPoll < 1) {
      return of(create(component, parameter, this, format("The '%s' parameter must have a value greater than 1",
                                                          POLLING_SOURCE_LIMIT_PARAMETER_NAME)));
    }
    return empty();
  }

}
