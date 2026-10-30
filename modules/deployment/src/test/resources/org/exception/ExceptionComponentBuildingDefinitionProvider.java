/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.exception;

import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromSimpleParameter;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromSimpleReferenceParameter;
import static com.zernova.runtime.dsl.api.component.TypeDefinition.fromType;
import com.zernova.functional.api.component.ThrowProcessor;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinitionProvider;

import java.util.ArrayList;
import java.util.List;


public class ExceptionComponentBuildingDefinitionProvider implements ComponentBuildingDefinitionProvider {

  private ComponentBuildingDefinition.Builder baseDefinition;

  @Override
  public void init() {
    baseDefinition = new ComponentBuildingDefinition.Builder().withNamespace("ex");
  }

  @Override
  public List<ComponentBuildingDefinition> getComponentBuildingDefinitions() {
    List<ComponentBuildingDefinition> componentBuildingDefinitions = new ArrayList<>();


    componentBuildingDefinitions.add(baseDefinition
                                             .withIdentifier("throw-exception")
                                             .withTypeDefinition(fromType(ThrowProcessor.class))
                                             .withSetterParameterDefinition("exception", fromSimpleParameter("exceptionClassName").build())
                                             .withSetterParameterDefinition("error", fromSimpleParameter("error").build())
                                             .withSetterParameterDefinition("count", fromSimpleParameter("count").withDefaultValue(-1).build()).build());
    return componentBuildingDefinitions;
  }

}

