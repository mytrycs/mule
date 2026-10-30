/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.source;

import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromChildConfiguration;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromFixedValue;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromReferenceObject;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromSimpleParameter;
import static com.zernova.runtime.dsl.api.component.TypeDefinition.fromType;
import static com.zernova.runtime.extension.api.ExtensionConstants.BACK_PRESSURE_STRATEGY_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.ExtensionConstants.PRIMARY_NODE_ONLY_PARAMETER_NAME;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.toBackPressureStrategy;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.core.api.streaming.CursorProviderFactory;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition.Builder;
import com.zernova.runtime.extension.api.dsl.syntax.DslElementSyntax;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.extension.api.property.BackPressureStrategyModelProperty;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionDefinitionParser;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionParsingContext;
import com.zernova.runtime.module.extension.internal.runtime.source.ExtensionMessageSource;

import java.util.List;
import java.util.Optional;

/**
 * An {@link ExtensionMessageSource} used to parse instances of {@link ExtensionMessageSource} instances through a
 * {@link SourceDefinitionParser}
 *
 * @since 4.0
 */
public class SourceDefinitionParser extends ExtensionDefinitionParser {

  private final ExtensionModel extensionModel;
  private final SourceModel sourceModel;
  private final DslElementSyntax sourceDsl;

  public SourceDefinitionParser(Builder definition, ExtensionModel extensionModel,
                                SourceModel sourceModel, DslSyntaxResolver dslSyntaxResolver,
                                ExtensionParsingContext parsingContext, Optional<ClassTypeLoader> typeLoader) {
    super(definition, dslSyntaxResolver, parsingContext, typeLoader);
    this.extensionModel = extensionModel;
    this.sourceModel = sourceModel;
    this.sourceDsl = dslSyntaxResolver.resolve(sourceModel);
  }

  @Override
  protected Builder doParse(Builder definitionBuilder) throws ConfigurationException {
    Builder finalBuilder =
        definitionBuilder.withIdentifier(sourceDsl.getElementName()).withTypeDefinition(fromType(ExtensionMessageSource.class))
            .withObjectFactoryType(ExtensionSourceObjectFactory.class)
            .withConstructorParameterDefinition(fromFixedValue(extensionModel).build())
            .withConstructorParameterDefinition(fromFixedValue(sourceModel).build())
            .withConstructorParameterDefinition(fromReferenceObject(ZernovaContext.class).build())
            .withSetterParameterDefinition("retryPolicyTemplate", fromChildConfiguration(RetryPolicyTemplate.class).build())
            .withSetterParameterDefinition(CURSOR_PROVIDER_FACTORY_FIELD_NAME,
                                           fromChildConfiguration(CursorProviderFactory.class).build());

    if (!sourceModel.runsOnPrimaryNodeOnly()) {
      finalBuilder = finalBuilder
          .withSetterParameterDefinition("primaryNodeOnly", fromSimpleParameter(PRIMARY_NODE_ONLY_PARAMETER_NAME).build());
    }

    if (sourceModel.getModelProperty(BackPressureStrategyModelProperty.class)
        .filter(bpmp -> bpmp.getSupportedModes().size() > 1)
        .isPresent()) {
      finalBuilder = finalBuilder
          .withSetterParameterDefinition("backPressureStrategy",
                                         fromSimpleParameter(BACK_PRESSURE_STRATEGY_PARAMETER_NAME,
                                                             v -> toBackPressureStrategy((String) v)).build());
    }

    List<ParameterGroupModel> inlineGroups = getInlineGroups(sourceModel);
    sourceModel.getErrorCallback().ifPresent(cb -> inlineGroups.addAll(getInlineGroups(cb)));
    sourceModel.getSuccessCallback().ifPresent(cb -> inlineGroups.addAll(getInlineGroups(cb)));

    parseParameters(getFlatParameters(inlineGroups, sourceModel.getAllParameterModels()));

    for (ParameterGroupModel group : inlineGroups) {
      parseInlineParameterGroup(group);
    }

    return finalBuilder;
  }
}
