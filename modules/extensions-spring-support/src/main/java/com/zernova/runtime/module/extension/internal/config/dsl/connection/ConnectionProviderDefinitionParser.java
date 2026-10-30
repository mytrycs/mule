/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.connection;

import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromChildConfiguration;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromFixedValue;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromReferenceObject;
import static com.zernova.runtime.dsl.api.component.TypeDefinition.fromType;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.api.config.PoolingProfile;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.retry.ReconnectionConfig;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition.Builder;
import com.zernova.runtime.extension.api.dsl.syntax.DslElementSyntax;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionDefinitionParser;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionParsingContext;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.authcode.AuthorizationCodeOAuthHandler;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.clientcredentials.ClientCredentialsOAuthHandler;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ocs.PlatformManagedOAuthHandler;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ConnectionProviderResolver;

import java.util.Optional;

/**
 * A {@link ExtensionDefinitionParser} for parsing {@link ConnectionProviderResolver} instances through a
 * {@link ConnectionProviderObjectFactory}
 *
 * @since 4.0
 */
public final class ConnectionProviderDefinitionParser extends ExtensionDefinitionParser {

  private final ConnectionProviderModel providerModel;
  private final ExtensionModel extensionModel;
  private final DslElementSyntax connectionDsl;

  public ConnectionProviderDefinitionParser(Builder definition, ConnectionProviderModel providerModel,
                                            ExtensionModel extensionModel, DslSyntaxResolver dslSyntaxResolver,
                                            ExtensionParsingContext parsingContext,
                                            Optional<ClassTypeLoader> typeLoader) {
    super(definition, dslSyntaxResolver, parsingContext, typeLoader);
    this.providerModel = providerModel;
    this.extensionModel = extensionModel;
    this.connectionDsl = dslSyntaxResolver.resolve(providerModel);
  }

  @Override
  protected Builder doParse(Builder definitionBuilder) throws ConfigurationException {
    Builder finalBuilder = definitionBuilder.withIdentifier(connectionDsl.getElementName())
        .withTypeDefinition(fromType(ConnectionProviderResolver.class))
        .withObjectFactoryType(ConnectionProviderObjectFactory.class)
        .withConstructorParameterDefinition(fromFixedValue(providerModel).build())
        .withConstructorParameterDefinition(fromFixedValue(extensionModel).build())
        .withConstructorParameterDefinition(fromReferenceObject(AuthorizationCodeOAuthHandler.class).build())
        .withConstructorParameterDefinition(fromReferenceObject(ClientCredentialsOAuthHandler.class).build())
        .withConstructorParameterDefinition(fromReferenceObject(PlatformManagedOAuthHandler.class).build())
        .withConstructorParameterDefinition(fromReferenceObject(ZernovaContext.class).build())
        .withSetterParameterDefinition("reconnectionConfig",
                                       fromChildConfiguration(ReconnectionConfig.class).build())
        .withSetterParameterDefinition("poolingProfile", fromChildConfiguration(PoolingProfile.class).build());

    parseParameters(providerModel);

    return finalBuilder;
  }
}
