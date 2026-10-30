/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tests.chains.api.config;

import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromChildCollectionConfiguration;
import static com.zernova.runtime.dsl.api.component.TypeDefinition.fromType;
import static com.zernova.tests.chains.api.config.TestProcessorChainsNamespaceInfoProvider.TEST_PROCESSOR_CHAINS_NAMESPACE;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.core.privileged.processor.CompositeProcessorChainRouter;
import com.zernova.runtime.core.privileged.processor.ProcessorChainRouter;
import com.zernova.runtime.core.privileged.processor.objectfactory.MessageProcessorChainObjectFactory;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinitionProvider;

import java.util.ArrayList;
import java.util.List;

public class TestProcessorChainsComponentBuildingDefinitionProvider implements ComponentBuildingDefinitionProvider {

  @Override
  public void init() {}

  @Override
  public List<ComponentBuildingDefinition> getComponentBuildingDefinitions() {
    List<ComponentBuildingDefinition> definitions = new ArrayList<>();
    ComponentBuildingDefinition.Builder baseBuilder =
        new ComponentBuildingDefinition.Builder().withNamespace(TEST_PROCESSOR_CHAINS_NAMESPACE);

    definitions.add(baseBuilder.withIdentifier("composite-processor-chain-router")
        .withTypeDefinition(fromType(CompositeProcessorChainRouter.class))
        .withSetterParameterDefinition("processorChains", fromChildCollectionConfiguration(Object.class).build())
        .build());

    definitions.add(baseBuilder.withIdentifier("chain")
        .withTypeDefinition(fromType(Component.class))
        .withObjectFactoryType(MessageProcessorChainObjectFactory.class)
        .withSetterParameterDefinition("messageProcessors", fromChildCollectionConfiguration(Object.class).build())
        .build());

    definitions.add(baseBuilder.withIdentifier("processor-chain-router")
        .withTypeDefinition(fromType(ProcessorChainRouter.class))
        .withSetterParameterDefinition("processors", fromChildCollectionConfiguration(Object.class).build())
        .build());

    return definitions;
  }

}

