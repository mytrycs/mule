/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.config;

import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromChildConfiguration;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromFixedValue;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromReferenceObject;
import static com.zernova.runtime.dsl.api.component.TypeDefinition.fromType;
import static com.zernova.runtime.extension.api.util.ExtensionModelUtils.supportsConnectivity;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition.Builder;
import com.zernova.runtime.extension.api.dsl.syntax.DslElementSyntax;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.extension.api.runtime.ExpirationPolicy;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionDefinitionParser;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionParsingContext;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ConnectionProviderResolver;

import java.util.Optional;

/**
 * A {@link ExtensionDefinitionParser} for parsing {@link ConfigurationProvider} instances through a
 * {@link ConfigurationProviderObjectFactory}
 *
 * @since 4.0
 */
public final class ConfigurationDefinitionParser extends ExtensionDefinitionParser {

  private final ExtensionModel extensionModel;
  private final ConfigurationModel configurationModel;
  private final DslElementSyntax configDsl;

  public ConfigurationDefinitionParser(Builder definition, ExtensionModel extensionModel,
                                       ConfigurationModel configurationModel,
                                       DslSyntaxResolver dslResolver,
                                       ExtensionParsingContext parsingContext,
                                       Optional<ClassTypeLoader> typeLoader) {
    super(definition, dslResolver, parsingContext, typeLoader);
    this.extensionModel = extensionModel;
    this.configurationModel = configurationModel;
    this.configDsl = dslResolver.resolve(configurationModel);
  }

  @Override
  protected Builder doParse(Builder definitionBuilder) throws ConfigurationException {
    Builder finalBuilder =
        definitionBuilder.withIdentifier(configDsl.getElementName()).withTypeDefinition(fromType(ConfigurationProvider.class))
            .withObjectFactoryType(ConfigurationProviderObjectFactory.class)
            .withConstructorParameterDefinition(fromFixedValue(extensionModel).build())
            .withConstructorParameterDefinition(fromFixedValue(configurationModel).build())
            .withConstructorParameterDefinition(fromReferenceObject(ZernovaContext.class).build())
            .withSetterParameterDefinition("expirationPolicy", fromChildConfiguration(ExpirationPolicy.class).build());

    parseParameters(configurationModel);
    finalBuilder = parseConnectionProvider(finalBuilder);

    return finalBuilder;
  }

  private Builder parseConnectionProvider(Builder definitionBuilder) {
    if (supportsConnectivity(extensionModel, configurationModel)) {
      return definitionBuilder.withSetterParameterDefinition("requiresConnection", fromFixedValue(true).build())
          .withSetterParameterDefinition("connectionProviderResolver",
                                         fromChildConfiguration(ConnectionProviderResolver.class).build());
    } else {
      return definitionBuilder;
    }
  }

}
