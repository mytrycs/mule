/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.authcode;

import static com.zernova.runtime.core.internal.event.NullEventFactory.getNullEvent;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.ACCESS_TOKEN_URL_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.AFTER_FLOW_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.AUTHORIZATION_URL_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.BEFORE_FLOW_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.CALLBACK_PATH_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.CONSUMER_KEY_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.CONSUMER_SECRET_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.EXTERNAL_CALLBACK_URL_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.LISTENER_CONFIG_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.LOCAL_AUTHORIZE_PATH_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.OAUTH_AUTHORIZATION_CODE_GROUP_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.OAUTH_CALLBACK_GROUP_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.RESOURCE_OWNER_ID_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.connectivity.oauth.ExtensionOAuthConstants.SCOPES_PARAMETER_NAME;
import static com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ExtensionsOAuthUtils.getCallbackValuesExtractors;

import com.zernova.runtime.api.config.PoolingProfile;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.retry.ReconnectionConfig;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import com.zernova.runtime.extension.api.connectivity.oauth.AuthorizationCodeGrantType;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.BaseOAuthConnectionProviderObjectBuilder;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.CustomOAuthParameters;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.OAuthObjectStoreConfig;
import com.zernova.runtime.module.extension.internal.runtime.resolver.MapValueResolver;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A specialization of {@link BaseOAuthConnectionProviderObjectBuilder} to wrap the {@link ConnectionProvider} into
 * {@link AuthorizationCodeConnectionProviderWrapper} instances.
 *
 * @since 4.0
 */
public class AuthorizationCodeConnectionProviderObjectBuilder<C> extends BaseOAuthConnectionProviderObjectBuilder<C>
    implements Startable {

  private final AuthorizationCodeOAuthHandler authCodeHandler;
  private final AuthorizationCodeGrantType grantType;
  private final Map<Field, String> callbackValues;
  Supplier<Boolean> forceInvalidateStatusRetrievalSupplier;

  public AuthorizationCodeConnectionProviderObjectBuilder(ConnectionProviderModel providerModel,
                                                          ResolverSet resolverSet,
                                                          PoolingProfile poolingProfile,
                                                          ReconnectionConfig reconnectionConfig,
                                                          AuthorizationCodeGrantType grantType,
                                                          AuthorizationCodeOAuthHandler authCodeHandler,
                                                          ExtensionModel extensionModel,
                                                          ExpressionManager expressionManager,
                                                          ZernovaContext muleContext) {
    super(providerModel, resolverSet, poolingProfile, reconnectionConfig, extensionModel, expressionManager, muleContext);
    this.authCodeHandler = authCodeHandler;
    this.grantType = grantType;
    callbackValues = getCallbackValuesExtractors(providerModel);
    this.forceInvalidateStatusRetrievalSupplier = () -> !muleContext.getClusterId().isEmpty();
  }

  @Override
  public void start() throws ZernovaException {
    authCodeHandler.register(getInitialOAuthConfig());
  }

  @Override
  protected ConnectionProvider<C> doBuild(ResolverSetResult result) throws ZernovaException {
    ConnectionProvider<C> provider = super.doBuild(result);

    Map<String, String> authCodeParams = getAuthCodeParameterMap(result);

    CustomOAuthParameters customParameters = getCustomParameters(result);
    AuthorizationCodeConfig config = new AuthorizationCodeConfig(ownerConfigName,
                                                                 buildOAuthObjectStoreConfig(result),
                                                                 customParameters,
                                                                 callbackValues,
                                                                 grantType,
                                                                 buildOAuthCallbackConfig(result),
                                                                 authCodeParams.get(CONSUMER_KEY_PARAMETER_NAME),
                                                                 authCodeParams.get(CONSUMER_SECRET_PARAMETER_NAME),
                                                                 authCodeParams.get(AUTHORIZATION_URL_PARAMETER_NAME),
                                                                 authCodeParams.get(ACCESS_TOKEN_URL_PARAMETER_NAME),
                                                                 authCodeParams.get(SCOPES_PARAMETER_NAME),
                                                                 authCodeParams.get(RESOURCE_OWNER_ID_PARAMETER_NAME),
                                                                 authCodeParams.get(BEFORE_FLOW_PARAMETER_NAME),
                                                                 authCodeParams.get(AFTER_FLOW_PARAMETER_NAME));

    provider = new AuthorizationCodeConnectionProviderWrapper<>(provider,
                                                                config,
                                                                callbackValues,
                                                                authCodeHandler,
                                                                reconnectionConfig, forceInvalidateStatusRetrievalSupplier);
    return provider;
  }

  @Override
  public Pair<ConnectionProvider<C>, ResolverSetResult> build(ValueResolvingContext context) throws ZernovaException {
    ResolverSetResult result = resolverSet.resolve(context);
    ConnectionProvider<C> provider = super.doBuild(result);

    Map<String, String> authCodeParams = getAuthCodeParameterMap(context.getEvent());

    CustomOAuthParameters customParameters = getCustomParameters(result);
    AuthorizationCodeConfig config = new AuthorizationCodeConfig(ownerConfigName,
                                                                 buildOAuthObjectStoreConfig(context.getEvent()),
                                                                 customParameters,
                                                                 callbackValues,
                                                                 grantType,
                                                                 buildOAuthCallbackConfig(context.getEvent()),
                                                                 authCodeParams.get(CONSUMER_KEY_PARAMETER_NAME),
                                                                 authCodeParams.get(CONSUMER_SECRET_PARAMETER_NAME),
                                                                 authCodeParams.get(AUTHORIZATION_URL_PARAMETER_NAME),
                                                                 authCodeParams.get(ACCESS_TOKEN_URL_PARAMETER_NAME),
                                                                 authCodeParams.get(SCOPES_PARAMETER_NAME),
                                                                 authCodeParams.get(RESOURCE_OWNER_ID_PARAMETER_NAME),
                                                                 authCodeParams.get(BEFORE_FLOW_PARAMETER_NAME),
                                                                 authCodeParams.get(AFTER_FLOW_PARAMETER_NAME));

    provider = new AuthorizationCodeConnectionProviderWrapper<>(provider,
                                                                config,
                                                                callbackValues,
                                                                authCodeHandler,
                                                                reconnectionConfig, forceInvalidateStatusRetrievalSupplier);
    return new Pair<>(provider, result);
  }

  private Map<String, String> getAuthCodeParameterMap(ResolverSetResult result) {
    return (Map<String, String>) result.get(OAUTH_AUTHORIZATION_CODE_GROUP_NAME);
  }

  private Map<String, String> getAuthCodeParameterMap(CoreEvent event) throws ZernovaException {
    ValueResolver<?> valueResolver = resolverSet.getResolvers().get(OAUTH_AUTHORIZATION_CODE_GROUP_NAME);
    try (ValueResolvingContext context = getResolvingContextFor(event)) {
      return (Map<String, String>) valueResolver.resolve(context);
    }
  }

  private OAuthCallbackConfig buildOAuthCallbackConfig(CoreEvent event) throws ZernovaException {
    ValueResolver<?> valueResolver = resolverSet.getResolvers().get(OAUTH_CALLBACK_GROUP_NAME);
    try (ValueResolvingContext context = getResolvingContextFor(event)) {
      Map<String, Object> map = (Map<String, Object>) valueResolver.resolve(context);
      return buildOAuthCallbackConfig(map);
    }
  }

  private OAuthCallbackConfig buildOAuthCallbackConfig(ResolverSetResult result) {
    Map<String, Object> map = (Map<String, Object>) result.get(OAUTH_CALLBACK_GROUP_NAME);
    return buildOAuthCallbackConfig(map);
  }

  private OAuthCallbackConfig buildOAuthCallbackConfig(Map<String, Object> map) {
    return new OAuthCallbackConfig((String) map.get(LISTENER_CONFIG_PARAMETER_NAME),
                                   sanitizePath((String) map.get(CALLBACK_PATH_PARAMETER_NAME)),
                                   sanitizePath((String) map.get(LOCAL_AUTHORIZE_PATH_PARAMETER_NAME)),
                                   (String) map.get(EXTERNAL_CALLBACK_URL_PARAMETER_NAME));
  }

  protected AuthorizationCodeConfig getInitialOAuthConfig() throws ZernovaException {
    CoreEvent initialiserEvent = null;
    ValueResolvingContext ctxForConfig = null;
    ValueResolvingContext ctxForCallback = null;
    try {
      initialiserEvent = getNullEvent();
      ValueResolver<?> oauthAuthCodeGroup = resolverSet.getResolvers().get(OAUTH_AUTHORIZATION_CODE_GROUP_NAME);

      MapValueResolver mapResolver = staticOnly((MapValueResolver) oauthAuthCodeGroup);
      Map<String, String> authCodeParams = mapResolver.resolve(ctxForConfig);

      ctxForConfig = getResolvingContextFor(initialiserEvent);
      Optional<OAuthObjectStoreConfig> storeConfig = buildOAuthObjectStoreConfig(initialiserEvent);

      mapResolver = staticOnly((MapValueResolver) resolverSet.getResolvers().get(OAUTH_CALLBACK_GROUP_NAME));
      ctxForCallback = getResolvingContextFor(initialiserEvent);
      OAuthCallbackConfig callbackConfig = buildOAuthCallbackConfig(mapResolver.resolve(ctxForCallback));

      CustomOAuthParameters customParameters = getCustomParameters(initialiserEvent);
      return new AuthorizationCodeConfig(ownerConfigName,
                                         storeConfig,
                                         customParameters,
                                         callbackValues,
                                         grantType,
                                         callbackConfig,
                                         authCodeParams.get(CONSUMER_KEY_PARAMETER_NAME),
                                         authCodeParams.get(CONSUMER_SECRET_PARAMETER_NAME),
                                         authCodeParams.get(AUTHORIZATION_URL_PARAMETER_NAME),
                                         authCodeParams.get(ACCESS_TOKEN_URL_PARAMETER_NAME),
                                         authCodeParams.get(SCOPES_PARAMETER_NAME),
                                         authCodeParams.get(RESOURCE_OWNER_ID_PARAMETER_NAME),
                                         authCodeParams.get(BEFORE_FLOW_PARAMETER_NAME),
                                         authCodeParams.get(AFTER_FLOW_PARAMETER_NAME));
    } finally {
      if (initialiserEvent != null) {
        ((BaseEventContext) initialiserEvent.getContext()).success();
      }
      if (ctxForCallback != null) {
        ctxForCallback.close();
      }
      if (ctxForConfig != null) {
        ctxForConfig.close();
      }
    }
  }
}
