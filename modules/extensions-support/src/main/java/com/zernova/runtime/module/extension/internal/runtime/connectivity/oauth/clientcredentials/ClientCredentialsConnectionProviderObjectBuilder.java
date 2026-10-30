/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.clientcredentials;

import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.CLIENT_ID_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.CLIENT_SECRET_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.OAUTH_CLIENT_CREDENTIALS_GROUP_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.SCOPES_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.TOKEN_URL_PARAMETER_NAME;
import static com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ExtensionsOAuthUtils.getCallbackValuesExtractors;

import com.zernova.runtime.api.config.PoolingProfile;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.retry.ReconnectionConfig;
import com.zernova.runtime.extension.api.connectivity.oauth.ClientCredentialsGrantType;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.BaseOAuthConnectionProviderObjectBuilder;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.CustomOAuthParameters;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * A specialization of {@link BaseOAuthConnectionProviderObjectBuilder} to wrap the {@link ConnectionProvider} into
 * {@link ClientCredentialsConnectionProviderWrapper} instances.
 *
 * @since 4.2.1
 */
public class ClientCredentialsConnectionProviderObjectBuilder<C> extends BaseOAuthConnectionProviderObjectBuilder<C> {

  private final ClientCredentialsOAuthHandler clientCredentialsHandler;
  private final ClientCredentialsGrantType grantType;
  private final Map<Field, String> callbackValues;

  public ClientCredentialsConnectionProviderObjectBuilder(ConnectionProviderModel providerModel,
                                                          ResolverSet resolverSet,
                                                          PoolingProfile poolingProfile,
                                                          ReconnectionConfig reconnectionConfig,
                                                          ClientCredentialsGrantType grantType,
                                                          ClientCredentialsOAuthHandler clientCredentialsHandler,
                                                          ExtensionModel extensionModel,
                                                          ExpressionManager expressionManager,
                                                          ZernovaContext muleContext) {
    super(providerModel, resolverSet, poolingProfile, reconnectionConfig, extensionModel, expressionManager, muleContext);
    this.clientCredentialsHandler = clientCredentialsHandler;
    this.grantType = grantType;
    callbackValues = getCallbackValuesExtractors(providerModel);
  }

  @Override
  protected ConnectionProvider<C> doBuild(ResolverSetResult result) throws ZernovaException {
    ConnectionProvider<C> provider = super.doBuild(result);
    provider = wrapProvider(result, provider, getClientCredentialsParams(result));

    return provider;
  }

  @Override
  public Pair<ConnectionProvider<C>, ResolverSetResult> build(ValueResolvingContext context) throws ZernovaException {
    ResolverSetResult result = resolverSet.resolve(context);
    ConnectionProvider<C> provider = super.doBuild(result);

    provider = wrapProvider(result, provider, getClientCredentialsParams(context.getEvent()));
    return new Pair<>(provider, result);
  }

  private ConnectionProvider<C> wrapProvider(ResolverSetResult result, ConnectionProvider<C> provider,
                                             Map<String, String> clientCredentialsParams) {
    CustomOAuthParameters customParameters = getCustomParameters(result);

    ClientCredentialsConfig config = new ClientCredentialsConfig(ownerConfigName,
                                                                 buildOAuthObjectStoreConfig(result),
                                                                 customParameters,
                                                                 callbackValues,
                                                                 clientCredentialsParams.get(CLIENT_ID_PARAMETER_NAME),
                                                                 clientCredentialsParams.get(CLIENT_SECRET_PARAMETER_NAME),
                                                                 clientCredentialsParams.get(TOKEN_URL_PARAMETER_NAME),
                                                                 clientCredentialsParams.get(SCOPES_PARAMETER_NAME),
                                                                 grantType.getCredentialsPlacement(),
                                                                 grantType);


    provider = new ClientCredentialsConnectionProviderWrapper<>(provider,
                                                                config,
                                                                callbackValues,
                                                                clientCredentialsHandler,
                                                                reconnectionConfig);
    return provider;
  }

  private Map<String, String> getClientCredentialsParams(ResolverSetResult result) {
    return (Map<String, String>) result.get(OAUTH_CLIENT_CREDENTIALS_GROUP_NAME);
  }

  private Map<String, String> getClientCredentialsParams(CoreEvent event) throws ZernovaException {
    ValueResolver<?> valueResolver = resolverSet.getResolvers().get(OAUTH_CLIENT_CREDENTIALS_GROUP_NAME);
    try (ValueResolvingContext context = getResolvingContextFor(event)) {
      return (Map<String, String>) valueResolver.resolve(context);
    }
  }
}
