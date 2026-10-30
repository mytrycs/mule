/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.config.provider;

import static com.zernova.runtime.config.internal.dsl.processor.xml.provider.OperationDslNamespaceInfoProvider.OPERATION_DSL_NAMESPACE;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromChildCollectionConfiguration;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromChildConfiguration;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromReferenceObject;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromSimpleParameter;
import static com.zernova.runtime.dsl.api.component.TypeDefinition.fromType;

import static java.util.Arrays.asList;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinitionProvider;
import com.zernova.runtime.extension.api.model.deprecated.ImmutableDeprecationModel;
import com.zernova.runtime.module.extension.mule.internal.construct.Operation;
import com.zernova.runtime.module.extension.mule.api.processor.ZernovaSdkRaiseErrorProcessor;
import com.zernova.runtime.module.extension.mule.internal.config.factory.DefaultOperationObjectFactory;

import java.util.List;

/**
 * Definition provider for the {@code operation:def} element.
 *
 * @since 4.5.0
 */
public class OperationDslBuildingDefinitionProvider implements ComponentBuildingDefinitionProvider {

  private static ComponentBuildingDefinition.Builder baseDefinition =
      new ComponentBuildingDefinition.Builder().withNamespace(OPERATION_DSL_NAMESPACE);

  @Override
  public void init() {}

  @Override
  public List<ComponentBuildingDefinition> getComponentBuildingDefinitions() {
    return asList(
                  baseDefinition.withIdentifier("def")
                      .withTypeDefinition(fromType(Operation.class))
                      .withObjectFactoryType(DefaultOperationObjectFactory.class)
                      .withConstructorParameterDefinition(fromSimpleParameter("name").build())
                      .withConstructorParameterDefinition(fromReferenceObject(ExtensionManager.class).build())
                      .withConstructorParameterDefinition(fromReferenceObject(ZernovaContext.class).build())
                      .withSetterParameterDefinition("body", fromChildConfiguration(OperationBody.class).build())
                      .withSetterParameterDefinition("deprecated",
                                                     fromChildConfiguration(ImmutableDeprecationModel.class).build())
                      .build(),
                  baseDefinition.withIdentifier("body")
                      .withTypeDefinition(fromType(OperationBody.class))
                      .withSetterParameterDefinition("processors", fromChildCollectionConfiguration(Processor.class).build())
                      .build(),
                  baseDefinition.withIdentifier("raise-error")
                      .withTypeDefinition(fromType(ZernovaSdkRaiseErrorProcessor.class))
                      .withSetterParameterDefinition("type", fromSimpleParameter("type").build())
                      .withSetterParameterDefinition("cause", fromSimpleParameter("cause").build())
                      .withSetterParameterDefinition("description", fromSimpleParameter("description").build())
                      .build());
  }

  public static class OperationBody extends AbstractComponent {

    private List<Processor> processors;

    public List<Processor> getProcessors() {
      return processors;
    }

    public void setProcessors(List<Processor> processors) {
      this.processors = processors;
    }
  }
}
