/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.oauth.api.builder;

import com.zernova.api.annotation.NoImplement;
import com.zernova.oauth.client.api.AuthorizationCodeRequest;
import com.zernova.oauth.client.api.builder.AuthorizationCodeDanceCallbackContext;
import com.zernova.oauth.client.api.builder.ClientCredentialsLocation;
import com.zernova.oauth.client.api.listener.AuthorizationCodeListener;
import com.zernova.oauth.client.api.state.ResourceOwnerOAuthContext;
import com.zernova.runtime.api.tls.TlsContextFactory;
import com.zernova.runtime.api.util.MultiMap;
import com.zernova.runtime.http.api.server.HttpServer;

import java.net.URL;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides compatibility with version 1.x of the mule-oauth-client, which is a transitive api of the service api.
 *
 * @deprecated since 1.5, use {@link com.zernova.oauth.client.api.builder.OAuthAuthorizationCodeDancerBuilder} from
 *             {@code mule-oauth-client 2.x}.
 */
@Deprecated
@NoImplement
public interface OAuthAuthorizationCodeDancerBuilder
    extends com.zernova.oauth.client.api.builder.OAuthAuthorizationCodeDancerBuilder,
    com.zernova.runtime.oauth.api.builder.OAuthDancerBuilder<com.zernova.oauth.client.api.AuthorizationCodeOAuthDancer> {

  /**
   * @deprecated since 4.2.0. Use {@link OAuthAuthorizationCodeDancerBuilder#withClientCredentialsIn(ClientCredentialsLocation)}
   *             instead.
   */
  @Override
  @Deprecated
  OAuthAuthorizationCodeDancerBuilder encodeClientCredentialsInBody(boolean encodeClientCredentialsInBody);

  @Override
  OAuthAuthorizationCodeDancerBuilder localCallback(URL localCallbackUrl);

  OAuthAuthorizationCodeDancerBuilder localCallback(URL localCallbackUrl, TlsContextFactory tlsContextFactory);

  @Override
  OAuthAuthorizationCodeDancerBuilder localCallback(HttpServer server, String localCallbackConfigPath);

  @Override
  OAuthAuthorizationCodeDancerBuilder localAuthorizationUrlPath(String path);

  @Override
  OAuthAuthorizationCodeDancerBuilder localAuthorizationUrlResourceOwnerId(String localAuthorizationUrlResourceOwnerIdExpr);

  @Override
  OAuthAuthorizationCodeDancerBuilder customParameters(Map<String, String> customParameters);

  @Override
  OAuthAuthorizationCodeDancerBuilder customParameters(Supplier<Map<String, String>> customParameters);

  @Override
  OAuthAuthorizationCodeDancerBuilder customHeaders(Map<String, String> customHeaders);

  @Override
  OAuthAuthorizationCodeDancerBuilder customHeaders(Supplier<Map<String, String>> customHeaders);

  @Override
  OAuthAuthorizationCodeDancerBuilder state(String stateExpr);

  @Override
  OAuthAuthorizationCodeDancerBuilder authorizationUrl(String authorizationUrl);

  @Override
  OAuthAuthorizationCodeDancerBuilder externalCallbackUrl(String externalCallbackUrl);

  @Override
  OAuthAuthorizationCodeDancerBuilder beforeDanceCallback(Function<AuthorizationCodeRequest, AuthorizationCodeDanceCallbackContext> callback);

  @Override
  OAuthAuthorizationCodeDancerBuilder afterDanceCallback(BiConsumer<AuthorizationCodeDanceCallbackContext, ResourceOwnerOAuthContext> callback);

  @Override
  OAuthAuthorizationCodeDancerBuilder addListener(AuthorizationCodeListener listener);

  OAuthAuthorizationCodeDancerBuilder addListener(com.zernova.runtime.oauth.api.listener.AuthorizationCodeListener listener);

  @Override
  OAuthAuthorizationCodeDancerBuilder addAdditionalRefreshTokenRequestParameters(MultiMap<String, String> additionalParameters);

  @Override
  OAuthAuthorizationCodeDancerBuilder addAdditionalRefreshTokenRequestHeaders(MultiMap<String, String> additionalHeaders);

  @Override
  OAuthAuthorizationCodeDancerBuilder includeRedirectUriInRefreshTokenRequest(boolean includeRedirectUriInRefreshTokenRequest);
}
