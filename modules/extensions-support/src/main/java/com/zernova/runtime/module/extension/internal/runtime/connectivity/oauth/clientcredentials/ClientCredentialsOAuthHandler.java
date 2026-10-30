/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.clientcredentials;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ExtensionsOAuthUtils.toCredentialsLocation;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

import com.zernova.oauth.client.api.ClientCredentialsOAuthDancer;
import com.zernova.oauth.client.api.builder.OAuthClientCredentialsDancerBuilder;
import com.zernova.oauth.client.api.listener.ClientCredentialsListener;
import com.zernova.oauth.client.api.state.ResourceOwnerOAuthContext;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.core.api.util.func.CheckedFunction;
import com.zernova.runtime.extension.api.connectivity.oauth.ClientCredentialsGrantType;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.OAuthHandler;
import com.zernova.runtime.module.extension.internal.store.LazyObjectStoreToMapAdapter;
import com.zernova.runtime.oauth.api.OAuthService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@link OAuthHandler} implementation for the client credentials grant type
 *
 * @since 4.2.1
 */
public class ClientCredentialsOAuthHandler extends OAuthHandler<ClientCredentialsOAuthDancer> {

  /**
   * Becomes aware of the given {@code config} and makes sure that the access token callback and authorization endpoints are
   * provisioned.
   *
   * @param config an {@link ClientCredentialsConfig}
   */
  public ClientCredentialsOAuthDancer register(ClientCredentialsConfig config) {
    return register(config, emptyList());
  }

  public ClientCredentialsOAuthDancer register(ClientCredentialsConfig config, List<ClientCredentialsListener> listeners) {
    return dancers.computeIfAbsent(config.getConfigIdentifier(),
                                   (CheckedFunction<String, ClientCredentialsOAuthDancer>) k -> createDancer(config, listeners));
  }

  /**
   * Performs the refresh token flow
   *
   * @param config a registered {@link ClientCredentialsConfig}
   */
  public void refreshToken(ClientCredentialsConfig config) {
    ClientCredentialsOAuthDancer dancer = dancers.get(config.getConfigIdentifier());

    try {
      dancer.refreshToken().get();
    } catch (Exception e) {
      throw new ZernovaRuntimeException(
                                     createStaticMessage(format("Could not refresh token for config '%s'",
                                                                config.getOwnerConfigName())),
                                     e);
    }
  }

  /**
   * Retrieves the {@link ResourceOwnerOAuthContext} for the given {@code config}. If no such context yet exists, then it performs
   * the OAuth authorization and returns the resulting context.
   *
   * @param config a {@link ClientCredentialsConfig}
   * @return the {@link ResourceOwnerOAuthContext} for the given {@code config}.
   */
  public ResourceOwnerOAuthContext getOAuthContext(ClientCredentialsConfig config) {
    ClientCredentialsOAuthDancer dancer = dancers.get(config.getConfigIdentifier());
    if (dancer == null) {
      throw new IllegalStateException(
                                      format("Client Credentials dancer for config '%s' not yet registered",
                                             config.getOwnerConfigName()));
    }

    ResourceOwnerOAuthContext contextForResourceOwner = dancer.getContext();

    if (contextForResourceOwner == null || contextForResourceOwner.getAccessToken() == null) {
      try {
        dancer.accessToken().get();
        contextForResourceOwner = dancer.getContext();
      } catch (Exception e) {
        throw new ZernovaRuntimeException(
                                       createStaticMessage(format("Could not obtain access token for config '%s'",
                                                                  config.getOwnerConfigName())),
                                       e);
      }
    }

    return contextForResourceOwner;
  }

  /**
   * Invalidates the OAuth state associated to the given {@code config}
   *
   * @param config a registered {@link ClientCredentialsConfig}
   */
  public void invalidate(ClientCredentialsConfig config) {
    ClientCredentialsOAuthDancer dancer = dancers.get(config.getConfigIdentifier());
    if (dancer == null) {
      return;
    }

    dancer.invalidateContext();
  }

  private ClientCredentialsOAuthDancer createDancer(ClientCredentialsConfig config, List<ClientCredentialsListener> listeners)
      throws ZernovaException {
    checkArgument(listeners != null, "listeners cannot be null");

    OAuthClientCredentialsDancerBuilder dancerBuilder =
        getOAuthService().get().clientCredentialsGrantTypeDancerBuilder(lockFactory,
                                                                        new LazyObjectStoreToMapAdapter(
                                                                                                        () -> objectStoreLocator
                                                                                                            .apply(config)),
                                                                        expressionEvaluator);

    final ClientCredentialsGrantType grantType = config.getGrantType();

    dancerBuilder
        .name(config.getOwnerConfigName())
        .encoding(artifactEncoding.getDefaultEncoding())
        .clientCredentials(config.getClientId(), config.getClientSecret())
        .tokenUrl(config.getTokenUrl())
        .responseExpiresInExpr(grantType.getExpirationRegex())
        .responseAccessTokenExpr(grantType.getAccessTokenExpr())
        .withClientCredentialsIn(toCredentialsLocation(grantType.getCredentialsPlacement()))
        .resourceOwnerIdTransformer(ownerId -> ownerId + "-" + config.getOwnerConfigName() + "-" + generateId(config));

    String scopes = config.getScope()
        .orElseGet(() -> grantType.getDefaultScopes().orElse(null));

    if (scopes != null) {
      dancerBuilder.scopes(scopes);
    }

    dancerBuilder
        .customParameters(config.getCustomQueryParameters())
        .customHeaders(config.getCustomHeaders())
        .customBodyParameters(config.getCustomBodyParameters())
        .customParametersExtractorsExprs(getParameterExtractors(config));

    listeners.forEach(dancerBuilder::addListener);

    ClientCredentialsOAuthDancer dancer = dancerBuilder.build();

    if (started) {
      start(dancer);
    }

    return dancer;
  }

  protected Integer generateId(ClientCredentialsConfig config) {
    return Objects.hash(config.getOwnerConfigName(), config.getClientId(), config.getClientSecret(), config.getTokenUrl(),
                        config.getScope(), config.getCustomQueryParameters(), config.getCustomHeaders());
  }

  protected Map<String, ClientCredentialsOAuthDancer> getDancers() {
    return dancers;
  }

  protected LazyValue<OAuthService> getOAuthService() {
    return oauthService;
  }
}
