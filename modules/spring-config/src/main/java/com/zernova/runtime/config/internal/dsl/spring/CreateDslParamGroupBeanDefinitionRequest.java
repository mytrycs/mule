/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.dsl.spring;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.ComponentParameterAst;
import com.zernova.runtime.config.internal.dsl.model.SpringComponentModel;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition;

import java.util.Collection;
import java.util.List;

public class CreateDslParamGroupBeanDefinitionRequest extends CreateBeanDefinitionRequest {

  private final ComponentAst paramOwnerComponent;
  private final ParameterGroupModel paramGroupModel;

  public CreateDslParamGroupBeanDefinitionRequest(ParameterGroupModel paramGroupModel, List<ComponentAst> componentHierarchy,
                                                  Collection<SpringComponentModel> paramsModels,
                                                  ComponentAst paramOwnerComponent,
                                                  ComponentBuildingDefinition<?> componentBuildingDefinition,
                                                  ComponentIdentifier paramComponentIdentifier) {
    super(componentHierarchy, null, paramsModels, componentBuildingDefinition,
          paramComponentIdentifier);
    this.paramGroupModel = paramGroupModel;
    this.paramOwnerComponent = paramOwnerComponent;
  }

  @Override
  public ComponentAst resolveConfigurationComponent() {
    return null;
  }

  public ComponentAst getParamOwnerComponent() {
    return paramOwnerComponent;
  }

  public ParameterGroupModel getParamGroupModel() {
    return paramGroupModel;
  }

  @Override
  public ComponentParameterAst getParameter(String parameterName) {
    return paramOwnerComponent.getParameter(paramGroupModel.getName(), parameterName);
  }
}
