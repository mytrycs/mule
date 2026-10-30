/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static com.zernova.metadata.api.utils.MetadataTypeUtils.getTypeId;
import static com.zernova.runtime.ast.api.util.ComponentAstPredicatesFactory.currentElemement;
import static com.zernova.runtime.extension.api.util.ExtensionModelUtils.getGroupAndParametersPairs;
import static com.zernova.runtime.ast.api.validation.Validation.Level.ERROR;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.api.scheduler.SchedulingStrategy;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.validation.Validation;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@IgnoreOnLazyInit
public class PollingSourceHasSchedulingStrategy implements Validation {

  @Override
  public String getName() {
    return "Polling source has scheduling strategy set";
  }

  @Override
  public String getDescription() {
    return "Polling source has scheduling strategy set";
  }

  @Override
  public Level getLevel() {
    return ERROR;
  }

  @Override
  public Predicate<List<ComponentAst>> applicable() {
    return currentElemement(component -> component.getModel(SourceModel.class)
        .flatMap(srcModel -> srcModel.getAllParameterModels().stream()
            .filter(pm -> isScheduler(pm.getType()))
            .findAny())
        .isPresent());
  }

  private boolean isScheduler(MetadataType type) {
    return getTypeId(type)
        .filter(typeId -> SchedulingStrategy.class.getName().equals(typeId))
        .isPresent();
  }

  @Override
  public Optional<ValidationResultItem> validate(ComponentAst component, ArtifactAst artifact) {
    final Pair<ParameterGroupModel, ParameterModel> schedulingStrategyParamModel = component.getModel(SourceModel.class)
        .flatMap(srcModel -> getGroupAndParametersPairs(srcModel)
            .filter(pm -> isScheduler(pm.getSecond().getType()))
            .findAny())
        .get();

    if (component
        .getParameter(schedulingStrategyParamModel.getFirst().getName(), schedulingStrategyParamModel.getSecond().getName())
        .getValue().getRight() == null) {
      return of(ValidationResultItem.create(component, this, "The scheduling strategy has not been configured."));
    } else {
      return empty();
    }
  }

}
