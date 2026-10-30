/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.connection;

import static com.zernova.runtime.core.api.retry.ReconnectionConfig.defaultReconnectionConfig;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getClassLoader;

import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.runtime.api.config.PoolingProfile;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.util.Reference;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.retry.ReconnectionConfig;
import com.zernova.runtime.extension.api.connectivity.oauth.AuthorizationCodeGrantType;
import com.zernova.runtime.extension.api.connectivity.oauth.ClientCredentialsGrantType;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthGrantType;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthGrantTypeVisitor;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthModelProperty;
import com.zernova.runtime.extension.api.connectivity.oauth.PlatformManagedOAuthGrantType;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.internal.config.dsl.AbstractExtensionObjectFactory;
import com.zernova.runtime.module.extension.internal.runtime.config.BaseConnectionProviderObjectBuilder;
import com.zernova.runtime.module.extension.internal.runtime.config.DefaultConnectionProviderObjectBuilder;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.authcode.AuthorizationCodeConnectionProviderObjectBuilder;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.authcode.AuthorizationCodeOAuthHandler;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.clientcredentials.ClientCredentialsConnectionProviderObjectBuilder;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.clientcredentials.ClientCredentialsOAuthHandler;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ocs.PlatformManagedOAuthConnectionProviderObjectBuilder;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ocs.PlatformManagedOAuthHandler;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ConnectionProviderResolver;

import java.util.concurrent.Callable;

import jakarta.inject.Inject;

/**
 * A {@link AbstractExtensionObjectFactory} that produces {@link ConnectionProviderResolver} instances
 *
 * @since 4.0
 */
public class ConnectionProviderObjectFactory extends AbstractExtensionObjectFactory<ConnectionProviderResolver> {

  private final ConnectionProviderModel providerModel;
  private final ExtensionModel extensionModel;
  private final AuthorizationCodeOAuthHandler authCodeHandler;
  private final ClientCredentialsOAuthHandler clientCredentialsHandler;
  private final PlatformManagedOAuthHandler platformManagedOAuthHandler;

  private PoolingProfile poolingProfile = null;
  private ReconnectionConfig reconnectionConfig = defaultReconnectionConfig();

  @Inject
  private ConfigurationProperties properties;

  @Inject
  private ZernovaContext muleContext;

  public ConnectionProviderObjectFactory(ConnectionProviderModel providerModel,
                                         ExtensionModel extensionModel,
                                         AuthorizationCodeOAuthHandler authCodeHandler,
                                         ClientCredentialsOAuthHandler clientCredentialsHandler,
                                         PlatformManagedOAuthHandler platformManagedOAuthHandler,
                                         ZernovaContext muleContext) {
    super(muleContext);
    this.providerModel = providerModel;
    this.extensionModel = extensionModel;
    this.authCodeHandler = authCodeHandler;
    this.clientCredentialsHandler = clientCredentialsHandler;
    this.platformManagedOAuthHandler = platformManagedOAuthHandler;
  }

  @Override
  public ConnectionProviderResolver doGetObject() {
    // TODO: W-11365218
    Callable<ResolverSet> callable = () -> getParametersResolver().getParametersAsResolverSet(providerModel, muleContext);
    ResolverSet resolverSet = withContextClassLoader(getClassLoader(extensionModel), callable);

    BaseConnectionProviderObjectBuilder builder;
    if (providerModel.getModelProperty(OAuthModelProperty.class).isPresent()) {
      builder = resolveOAuthBuilder(resolverSet);
    } else {
      builder = new DefaultConnectionProviderObjectBuilder(providerModel, resolverSet, poolingProfile,
                                                           reconnectionConfig,
                                                           extensionModel,
                                                           expressionManager,
                                                           muleContext);
    }

    return new ConnectionProviderResolver<>(builder, resolverSet, muleContext);
  }

  private BaseConnectionProviderObjectBuilder resolveOAuthBuilder(ResolverSet resolverSet) {
    OAuthGrantType grantType = providerModel.getModelProperty(OAuthModelProperty.class)
        .map(OAuthModelProperty::getGrantTypes)
        .get().get(0);

    Reference<BaseConnectionProviderObjectBuilder> builder = new Reference<>();

    grantType.accept(new OAuthGrantTypeVisitor() {

      @Override
      public void visit(AuthorizationCodeGrantType grantType) {
        builder.set(new AuthorizationCodeConnectionProviderObjectBuilder(providerModel,
                                                                         resolverSet,
                                                                         poolingProfile,
                                                                         reconnectionConfig,
                                                                         grantType,
                                                                         authCodeHandler,
                                                                         extensionModel,
                                                                         expressionManager,
                                                                         muleContext));
      }

      @Override
      public void visit(ClientCredentialsGrantType grantType) {
        builder.set(new ClientCredentialsConnectionProviderObjectBuilder(providerModel,
                                                                         resolverSet,
                                                                         poolingProfile,
                                                                         reconnectionConfig,
                                                                         grantType,
                                                                         clientCredentialsHandler,
                                                                         extensionModel,
                                                                         expressionManager,
                                                                         muleContext));
      }

      @Override
      public void visit(PlatformManagedOAuthGrantType grantType) {
        builder.set(new PlatformManagedOAuthConnectionProviderObjectBuilder(providerModel,
                                                                            resolverSet,
                                                                            poolingProfile,
                                                                            reconnectionConfig,
                                                                            grantType,
                                                                            platformManagedOAuthHandler,
                                                                            properties,
                                                                            extensionModel,
                                                                            expressionManager,
                                                                            muleContext));
      }
    });

    return builder.get();
  }

  public void setPoolingProfile(PoolingProfile poolingProfile) {
    this.poolingProfile = poolingProfile;
  }

  public void setReconnectionConfig(ReconnectionConfig reconnectionConfig) {
    this.reconnectionConfig = reconnectionConfig;
  }
}
