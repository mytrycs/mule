/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.parser;

import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromChildCollectionConfiguration;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromChildConfiguration;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromFixedValue;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromReferenceObject;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromSimpleParameter;
import static com.zernova.runtime.dsl.api.component.TypeDefinition.fromType;
import static com.zernova.runtime.extension.api.ExtensionConstants.ERROR_MAPPINGS_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.ExtensionConstants.TARGET_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.ExtensionConstants.TARGET_VALUE_PARAMETER_NAME;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.nested.NestableElementModel;
import com.zernova.runtime.api.meta.model.nested.NestedChainModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.core.api.streaming.CursorProviderFactory;
import com.zernova.runtime.core.internal.exception.EnrichedErrorMapping;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition.Builder;
import com.zernova.runtime.extension.api.dsl.syntax.DslElementSyntax;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.module.extension.internal.config.dsl.ComponentMessageProcessorObjectFactory;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionDefinitionParser;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionParsingContext;
import com.zernova.runtime.module.extension.internal.config.dsl.operation.OperationMessageProcessorObjectFactory;
import com.zernova.runtime.module.extension.internal.runtime.operation.ComponentMessageProcessor;
import com.zernova.runtime.module.extension.internal.runtime.operation.OperationMessageProcessor;

import java.util.List;
import java.util.Optional;

/**
 * A {@link ExtensionDefinitionParser} for parsing {@link OperationMessageProcessor} instances through a
 * {@link OperationMessageProcessorObjectFactory}
 *
 * @since 4.0
 */
public abstract class AbstractComponentDefinitionParser<T extends ComponentModel> extends ExtensionDefinitionParser {

  private final ExtensionModel extensionModel;
  private final T componentModel;
  private final DslElementSyntax operationDsl;

  public AbstractComponentDefinitionParser(Builder definition, ExtensionModel extensionModel,
                                           T componentModel,
                                           DslSyntaxResolver dslSyntaxResolver,
                                           ExtensionParsingContext parsingContext,
                                           Optional<ClassTypeLoader> typeLoader) {
    super(definition, dslSyntaxResolver, parsingContext, typeLoader);
    this.extensionModel = extensionModel;
    this.componentModel = componentModel;
    this.operationDsl = dslSyntaxResolver.resolve(componentModel);
  }

  @Override
  protected Builder doParse(Builder definitionBuilder)
      throws ConfigurationException {
    Builder finalBuilder = definitionBuilder.withIdentifier(operationDsl.getElementName())
        .withTypeDefinition(fromType(getMessageProcessorType()))
        .withObjectFactoryType(getMessageProcessorFactoryType())
        .withConstructorParameterDefinition(fromFixedValue(extensionModel).build())
        .withConstructorParameterDefinition(fromFixedValue(componentModel).build())
        .withConstructorParameterDefinition(fromReferenceObject(ZernovaContext.class).build())
        .withSetterParameterDefinition(CURSOR_PROVIDER_FACTORY_FIELD_NAME,
                                       fromChildConfiguration(CursorProviderFactory.class).build())
        .withSetterParameterDefinition("retryPolicyTemplate", fromChildConfiguration(RetryPolicyTemplate.class).build());

    if (hasOutputGroup()) {
      finalBuilder = finalBuilder
          .withSetterParameterDefinition(TARGET_PARAMETER_NAME,
                                         fromSimpleParameter(TARGET_PARAMETER_NAME).build())
          .withSetterParameterDefinition(TARGET_VALUE_PARAMETER_NAME,
                                         fromSimpleParameter(TARGET_VALUE_PARAMETER_NAME).build());
    }

    if (hasErrorMappingsGroup()) {
      finalBuilder = finalBuilder
          .withSetterParameterDefinition(ERROR_MAPPINGS_PARAMETER_NAME,
                                         fromChildCollectionConfiguration(EnrichedErrorMapping.class).build());
    }

    Optional<? extends NestableElementModel> nestedChain = componentModel.getNestedComponents().stream()
        .filter(NestedChainModel.class::isInstance)
        .findFirst();

    if (nestedChain.isPresent()) {
      // TODO MULE-13483: improve parsers to support things like [source, chainOfProcessors, errorHandler]
      // or [chainOfProcessors, errorHandler]
      finalBuilder = finalBuilder.withSetterParameterDefinition("nestedProcessors",
                                                                fromChildCollectionConfiguration(Processor.class).build());

      parseParameters(componentModel.getAllParameterModels());
    } else {
      List<ParameterGroupModel> inlineGroups = getInlineGroups(componentModel);
      parseParameters(getFlatParameters(inlineGroups, componentModel.getAllParameterModels()));
      for (ParameterGroupModel group : inlineGroups) {
        parseInlineParameterGroup(group);
      }

      // TODO MULE-13483
      parseNestedComponents(componentModel.getNestedComponents());
    }

    return finalBuilder;
  }

  protected boolean hasErrorMappingsGroup() {
    return false;
  }

  protected boolean hasOutputGroup() {
    return false;
  }

  protected abstract Class<? extends ComponentMessageProcessor> getMessageProcessorType();

  protected abstract Class<? extends ComponentMessageProcessorObjectFactory> getMessageProcessorFactoryType();

  public final T getComponentModel() {
    return componentModel;
  }
}
