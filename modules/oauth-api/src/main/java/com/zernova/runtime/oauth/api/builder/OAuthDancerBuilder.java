/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.oauth.api.builder;

import com.zernova.api.annotation.NoImplement;
import com.zernova.oauth.client.api.builder.ClientCredentialsLocation;
import com.zernova.runtime.api.tls.TlsContextFactory;
import com.zernova.runtime.http.api.client.HttpClient;
import com.zernova.runtime.http.api.client.proxy.ProxyConfig;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Function;

/**
 * Provides compatibility with version 1.x of the mule-oauth-client, which is a transitive api of the service api.
 *
 * @deprecated since 1.5, use {@link com.zernova.oauth.client.api.builder.OAuthDancerBuilder} from {@code mule-oauth-client 2.x}.
 */
@Deprecated
@NoImplement
public interface OAuthDancerBuilder<D> extends com.zernova.oauth.client.api.builder.OAuthDancerBuilder<D> {

  @Override
  OAuthDancerBuilder<D> name(String name);

  @Override
  OAuthDancerBuilder<D> clientCredentials(String clientId, String clientSecret);

  @Override
  OAuthDancerBuilder<D> withClientCredentialsIn(ClientCredentialsLocation clientCredentialsLocation);

  @Override
  OAuthDancerBuilder<D> tokenUrl(String tokenUrl);

  @Override
  OAuthDancerBuilder<D> tokenUrl(HttpClient httpClient, String tokenUrl);

  @Override
  OAuthDancerBuilder<D> tokenUrl(String tokenUrl, TlsContextFactory tlsContextFactory);

  @Override
  OAuthDancerBuilder<D> tokenUrl(String tokenUrl, ProxyConfig proxyConfig);

  @Override
  OAuthDancerBuilder<D> tokenUrl(String tokenUrl, TlsContextFactory tlsContextFactory, ProxyConfig proxyConfig);

  @Override
  OAuthDancerBuilder<D> scopes(String scopes);

  @Override
  OAuthDancerBuilder<D> encoding(Charset encoding);

  @Override
  OAuthDancerBuilder<D> responseAccessTokenExpr(String responseAccessTokenExpr);

  @Override
  OAuthDancerBuilder<D> responseRefreshTokenExpr(String responseRefreshTokenExpr);

  @Override
  OAuthDancerBuilder<D> responseExpiresInExpr(String responseExpiresInExpr);

  @Override
  OAuthDancerBuilder<D> customParametersExtractorsExprs(Map<String, String> customParamsExtractorsExprs);

  @Override
  OAuthDancerBuilder<D> resourceOwnerIdTransformer(Function<String, String> resourceOwnerIdTransformer);

  @Override
  D build();

}
