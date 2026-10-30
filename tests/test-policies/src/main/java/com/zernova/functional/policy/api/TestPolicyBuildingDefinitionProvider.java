/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.policy.api;

import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromChildCollectionConfiguration;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromChildConfiguration;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromSimpleParameter;
import static com.zernova.runtime.dsl.api.component.TypeDefinition.fromConfigurationAttribute;
import static com.zernova.runtime.dsl.api.component.TypeDefinition.fromType;

import com.zernova.runtime.core.api.policy.PolicyChain;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.internal.policy.DefaultPolicyInstance;
import com.zernova.runtime.core.internal.policy.PolicyNextActionMessageProcessor;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinitionProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a {@link ComponentBuildingDefinitionProvider} for the test policy module
 */
public class TestPolicyBuildingDefinitionProvider implements ComponentBuildingDefinitionProvider {

  private ComponentBuildingDefinition.Builder baseDefinition;

  @Override
  public void init() {
    baseDefinition = new ComponentBuildingDefinition.Builder()
        .withNamespace("test-policy");
  }

  @Override
  public List<ComponentBuildingDefinition> getComponentBuildingDefinitions() {
    List<ComponentBuildingDefinition> definitions = new ArrayList<>();

    definitions.add(baseDefinition.withIdentifier("proxy")
        .withTypeDefinition(fromType(DefaultPolicyInstance.class))
        .withSetterParameterDefinition("name", fromSimpleParameter("name").build())
        .withSetterParameterDefinition("sourcePolicyChain",
                                       fromChildConfiguration(PolicyChain.class).withWrapperIdentifier("source").build())
        .withSetterParameterDefinition("operationPolicyChain",
                                       fromChildConfiguration(PolicyChain.class).withWrapperIdentifier("operation").build())
        .build());

    definitions.add(baseDefinition.withIdentifier("source")
        .withTypeDefinition(fromType(PolicyChain.class))
        .withSetterParameterDefinition("processors", fromChildCollectionConfiguration(Processor.class).build()).build());

    definitions.add(baseDefinition.withIdentifier("operation")
        .withTypeDefinition(fromType(PolicyChain.class))
        .withSetterParameterDefinition("processors", fromChildCollectionConfiguration(Processor.class).build()).build());

    definitions.add(baseDefinition.withIdentifier("execute-next")
        .withTypeDefinition(fromType(PolicyNextActionMessageProcessor.class)).build());

    definitions.add(baseDefinition.withIdentifier("custom-processor")
        .withTypeDefinition(fromConfigurationAttribute("class")).build());

    return definitions;
  }
}
