/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.oauth.api;

import com.zernova.api.annotation.Experimental;
import com.zernova.api.annotation.NoImplement;
import com.zernova.oauth.client.api.AuthorizationCodeOAuthDancer;
import com.zernova.oauth.client.api.ClientCredentialsOAuthDancer;
import com.zernova.runtime.api.el.ZernovaExpressionLanguage;
import com.zernova.runtime.api.lock.LockFactory;
import com.zernova.runtime.api.service.Service;
import com.zernova.runtime.oauth.api.builder.OAuthPlatformManagedDancerBuilder;

import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Allows for creation of OAuth dancer implementations for the supported grant types:
 * <ul>
 * <li>Authorization Code Grant Type</li>
 * <li>Client Credentials</li>
 * </ul>
 *
 * @since 4.0
 */
@NoImplement
public interface OAuthService extends Service {

  /**
   * Creates a builder for an {@link ClientCredentialsOAuthDancer} for client credentials grant type. The dancer will use the
   * given {@code lockProvider} and {@code tokensStore} to manage its internal state.
   *
   * @param lockProvider        a factory for {@link Lock}s, uniquely identified by the {@code name} passed to
   *                            {@link LockFactory#createLock(String)}.
   * @param tokensStore         the repository for the tokens for the returned {@link ClientCredentialsOAuthDancer dancer}.
   * @param expressionEvaluator the object used to evaluate expressions.
   * @return a builder for a client-credentials grant type dancer.
   */
  <T> com.zernova.runtime.oauth.api.builder.OAuthClientCredentialsDancerBuilder clientCredentialsGrantTypeDancerBuilder(LockFactory lockProvider,
                                                                                                                     Map<String, T> tokensStore,
                                                                                                                     ZernovaExpressionLanguage expressionEvaluator);

  /**
   * Creates a builder for an {@link AuthorizationCodeOAuthDancer} for authorization code grant type. The dancer will use the
   * given {@code lockProvider} and {@code tokensStore} to manage its internal state.
   *
   * @param lockProvider        a factory for {@link Lock}s, uniquely identified by the {@code name} passed to
   *                            {@link LockFactory#createLock(String)}.
   * @param tokensStore         the repository for the tokens for the returned {@link AuthorizationCodeOAuthDancer dancer}.
   * @param expressionEvaluator the object used to evaluate expressions.
   * @return a builder for an authorization-code grant type dancer.
   */
  <T> com.zernova.runtime.oauth.api.builder.OAuthAuthorizationCodeDancerBuilder authorizationCodeGrantTypeDancerBuilder(LockFactory lockProvider,
                                                                                                                     Map<String, T> tokensStore,
                                                                                                                     ZernovaExpressionLanguage expressionEvaluator);

  /**
   * Creates a builder for a {@link PlatformManagedOAuthDancer} that obtains access tokens that are provisioned and managed by the
   * Anypoint Platform.
   * <p>
   * Platform Managed OAuth is an experimental feature. It will only be enabled on selected environments and scenarios. Backwards
   * compatibility is not guaranteed.
   *
   * @param lockFactory         a factory for {@link Lock}s, uniquely identified by the {@code name} passed to
   *                            {@link LockFactory#createLock(String)}.
   * @param tokensStore         the repository for the tokens for the returned {@link ClientCredentialsOAuthDancer dancer}.
   * @param expressionEvaluator the object used to evaluate expressions.
   * @return a builder for a client-credentials grant type dancer.
   * @since 4.3.0
   */
  @Experimental
  default <T> OAuthPlatformManagedDancerBuilder platformManagedOAuthDancerBuilder(LockFactory lockFactory,
                                                                                  Map<String, T> tokensStore,
                                                                                  ZernovaExpressionLanguage expressionEvaluator) {
    throw new UnsupportedOperationException("Platform Managed OAuth is only supported in the Enterprise Edition");
  }
}
