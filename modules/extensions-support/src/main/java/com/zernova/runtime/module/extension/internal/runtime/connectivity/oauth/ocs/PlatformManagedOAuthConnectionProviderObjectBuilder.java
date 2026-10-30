/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ocs;

import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.PLATFORM_MANAGED_CONNECTION_ID_PARAMETER_NAME;
import static com.zernova.runtime.module.extension.internal.ExtensionProperties.CONFIGURATION_MODEL_PROPERTY_NAME;
import static com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ocs.PlatformManagedOAuthConfig.from;

import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.runtime.api.config.PoolingProfile;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.api.util.Reference;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.retry.ReconnectionConfig;
import com.zernova.runtime.extension.api.connectivity.oauth.AuthorizationCodeGrantType;
import com.zernova.runtime.extension.api.connectivity.oauth.ClientCredentialsGrantType;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthGrantType;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthGrantTypeVisitor;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthModelProperty;
import com.zernova.runtime.extension.api.connectivity.oauth.PlatformManagedOAuthGrantType;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.BaseOAuthConnectionProviderObjectBuilder;

/**
 * A {@link BaseOAuthConnectionProviderObjectBuilder} implementation that yields {@link PlatformManagedOAuthConnectionProvider}
 * instances
 *
 * @param <C> the generic type of the produced connections
 * @since 4.3.0
 */
public class PlatformManagedOAuthConnectionProviderObjectBuilder<C> extends BaseOAuthConnectionProviderObjectBuilder<C> {

  private final PlatformManagedOAuthHandler platformHandler;
  private final PlatformManagedOAuthGrantType grantType;
  private final ConfigurationProperties configurationProperties;

  public PlatformManagedOAuthConnectionProviderObjectBuilder(ConnectionProviderModel providerModel,
                                                             ResolverSet resolverSet,
                                                             PoolingProfile poolingProfile,
                                                             ReconnectionConfig reconnectionConfig,
                                                             PlatformManagedOAuthGrantType grantType,
                                                             PlatformManagedOAuthHandler platformHandler,
                                                             ConfigurationProperties configurationProperties,
                                                             ExtensionModel extensionModel,
                                                             ExpressionManager expressionManager,
                                                             ZernovaContext muleContext) {
    super(PlatformManagedOAuthConnectionProvider.class, providerModel, resolverSet, poolingProfile, reconnectionConfig,
          extensionModel, expressionManager, muleContext);
    this.platformHandler = platformHandler;
    this.grantType = grantType;
    this.configurationProperties = configurationProperties;
  }

  @Override
  public Pair<ConnectionProvider<C>, ResolverSetResult> build(ValueResolvingContext context) throws ZernovaException {
    final ConfigurationModel configurationModel = (ConfigurationModel) context.getProperty(CONFIGURATION_MODEL_PROPERTY_NAME);
    final ResolverSetResult resolverSetResult = resolverSet.resolve(context);
    final Pair<ConnectionProviderModel, OAuthGrantType> delegateModel =
        getDelegateOAuthConnectionProviderModel(configurationModel);
    final String connectionId = (String) resolverSetResult.get(PLATFORM_MANAGED_CONNECTION_ID_PARAMETER_NAME);
    final PlatformManagedOAuthConfig config = from(ownerConfigName,
                                                   connectionId,
                                                   grantType,
                                                   extensionModel,
                                                   delegateModel.getFirst(),
                                                   delegateModel.getSecond(),
                                                   configurationProperties);

    ConnectionProvider<C> provider = new PlatformManagedOAuthConnectionProvider<>(config,
                                                                                  platformHandler,
                                                                                  reconnectionConfig,
                                                                                  poolingProfile);

    return new Pair<>(provider, resolverSetResult);
  }

  protected Pair<ConnectionProviderModel, OAuthGrantType> getDelegateOAuthConnectionProviderModel(ConfigurationModel configurationModel) {
    Reference<Pair<ConnectionProviderModel, OAuthGrantType>> authCodePair = new Reference<>();
    Reference<Pair<ConnectionProviderModel, OAuthGrantType>> clientCredentialsPair = new Reference<>();

    for (ConnectionProviderModel cpModel : configurationModel.getConnectionProviders()) {
      if (authCodePair.get() != null) {
        break;
      }

      OAuthModelProperty property = cpModel.getModelProperty(OAuthModelProperty.class).orElse(null);
      if (property != null) {
        property.getGrantTypes().get(0).accept(new OAuthGrantTypeVisitor() {

          @Override
          public void visit(AuthorizationCodeGrantType grantType) {
            authCodePair.set(new Pair<>(cpModel, grantType));
          }

          @Override
          public void visit(ClientCredentialsGrantType grantType) {
            clientCredentialsPair.set(new Pair<>(cpModel, grantType));
          }

          @Override
          public void visit(PlatformManagedOAuthGrantType grantType) {
            // no - op
          }
        });
      }
    }

    if (authCodePair.get() != null) {
      return authCodePair.get();
    } else {
      return clientCredentialsPair.get();
    }
  }
}
