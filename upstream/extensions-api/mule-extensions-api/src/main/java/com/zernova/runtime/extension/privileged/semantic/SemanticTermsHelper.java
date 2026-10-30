/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.privileged.semantic;

import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.ACCOUNT_ID;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.API_KEY;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.API_KEY_AUTH_CONNECTION;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.BASIC_AUTH_CONNECTION;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.CLIENT_ID;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.CLIENT_SECRET;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.CONNECTION_ID;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.CONNECTIVITY_PREFIX;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.DIGEST_AUTH_CONNECTION;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.DOMAIN;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.ENDPOINT;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.HOST;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.KERBEROS_AUTH_CONNECTION;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.NTLM_DOMAIN;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.NTLM_PROXY_CONFIGURATION;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.PASSWORD;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.PORT;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.PROXY_CONFIGURATION_TYPE;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.SECRET;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.SECRET_TOKEN;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.SECURITY_TOKEN;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.SESSION_ID;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.TENANT;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.TOKEN_ID;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.UNSECURED_CONNECTION;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.URL_PATH;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.URL_TEMPLATE;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.USERNAME;

import static java.util.stream.Collectors.toCollection;

import com.zernova.sdk.api.annotation.semantics.connectivity.ApiKeyAuth;
import com.zernova.sdk.api.annotation.semantics.connectivity.BasicAuth;
import com.zernova.sdk.api.annotation.semantics.connectivity.ConfiguresNtlmProxy;
import com.zernova.sdk.api.annotation.semantics.connectivity.ConfiguresProxy;
import com.zernova.sdk.api.annotation.semantics.connectivity.DigestAuth;
import com.zernova.sdk.api.annotation.semantics.connectivity.Domain;
import com.zernova.sdk.api.annotation.semantics.connectivity.Endpoint;
import com.zernova.sdk.api.annotation.semantics.connectivity.Host;
import com.zernova.sdk.api.annotation.semantics.connectivity.KerberosAuth;
import com.zernova.sdk.api.annotation.semantics.connectivity.NtlmDomain;
import com.zernova.sdk.api.annotation.semantics.connectivity.Port;
import com.zernova.sdk.api.annotation.semantics.connectivity.Unsecured;
import com.zernova.sdk.api.annotation.semantics.connectivity.Url;
import com.zernova.sdk.api.annotation.semantics.connectivity.UrlPath;
import com.zernova.sdk.api.annotation.semantics.security.AccountId;
import com.zernova.sdk.api.annotation.semantics.security.ApiKey;
import com.zernova.sdk.api.annotation.semantics.security.ClientId;
import com.zernova.sdk.api.annotation.semantics.security.ClientSecret;
import com.zernova.sdk.api.annotation.semantics.security.ConnectionId;
import com.zernova.sdk.api.annotation.semantics.security.Password;
import com.zernova.sdk.api.annotation.semantics.security.Secret;
import com.zernova.sdk.api.annotation.semantics.security.SecretToken;
import com.zernova.sdk.api.annotation.semantics.security.SecurityToken;
import com.zernova.sdk.api.annotation.semantics.security.SessionId;
import com.zernova.sdk.api.annotation.semantics.security.TenantIdentifier;
import com.zernova.sdk.api.annotation.semantics.security.TokenId;
import com.zernova.sdk.api.annotation.semantics.security.Username;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Helper class to assist in dealing with semantic terms.
 *
 * <b>THIS CLASS IS NOT API. Only to be used by Mule Runtime internals</b>
 *
 * @since 1.5.0
 */
public final class SemanticTermsHelper {

  /**
   * Maps SDK and Extension API annotations to a {@link Set} of semantic terms.
   */
  private static final Map<Class<? extends Annotation>, String> PARAMS_TERMS = new HashMap<>();
  private static final Map<Class<? extends Annotation>, String> CONNECTION_TERMS = new HashMap<>();


  static {
    CONNECTION_TERMS.put(ApiKeyAuth.class, API_KEY_AUTH_CONNECTION);
    CONNECTION_TERMS.put(BasicAuth.class, BASIC_AUTH_CONNECTION);
    CONNECTION_TERMS.put(DigestAuth.class, DIGEST_AUTH_CONNECTION);
    CONNECTION_TERMS.put(KerberosAuth.class, KERBEROS_AUTH_CONNECTION);
    CONNECTION_TERMS.put(Unsecured.class, UNSECURED_CONNECTION);

    PARAMS_TERMS.put(ConfiguresNtlmProxy.class, NTLM_PROXY_CONFIGURATION);
    PARAMS_TERMS.put(ConfiguresProxy.class, PROXY_CONFIGURATION_TYPE);
    PARAMS_TERMS.put(Domain.class, DOMAIN);
    PARAMS_TERMS.put(Endpoint.class, ENDPOINT);
    PARAMS_TERMS.put(Host.class, HOST);
    PARAMS_TERMS.put(NtlmDomain.class, NTLM_DOMAIN);
    PARAMS_TERMS.put(Port.class, PORT);
    PARAMS_TERMS.put(Url.class, URL_TEMPLATE);
    PARAMS_TERMS.put(UrlPath.class, URL_PATH);
    PARAMS_TERMS.put(AccountId.class, ACCOUNT_ID);
    PARAMS_TERMS.put(ApiKey.class, API_KEY);
    PARAMS_TERMS.put(ClientId.class, CLIENT_ID);
    PARAMS_TERMS.put(ClientSecret.class, CLIENT_SECRET);
    PARAMS_TERMS.put(ConnectionId.class, CONNECTION_ID);
    PARAMS_TERMS.put(Password.class, PASSWORD);
    PARAMS_TERMS.put(com.zernova.runtime.extension.api.annotation.param.display.Password.class, PASSWORD);
    PARAMS_TERMS.put(Secret.class, SECRET);
    PARAMS_TERMS.put(SecretToken.class, SECRET_TOKEN);
    PARAMS_TERMS.put(SecurityToken.class, SECURITY_TOKEN);
    PARAMS_TERMS.put(SessionId.class, SESSION_ID);
    PARAMS_TERMS.put(TenantIdentifier.class, TENANT);
    PARAMS_TERMS.put(TokenId.class, TOKEN_ID);
    PARAMS_TERMS.put(Username.class, USERNAME);
  }

  public static Set<String> getConnectionTermsFromAnnotations(Function<Class<? extends Annotation>, Boolean> predicate) {
    Set<String> collector = new LinkedHashSet<>();
    collectFromAnnotations(CONNECTION_TERMS, predicate, collector);

    return collector;
  }

  public static Set<String> getParameterTermsFromAnnotations(Function<Class<? extends Annotation>, Boolean> predicate) {
    Set<String> collector = new LinkedHashSet<>();
    collectFromAnnotations(PARAMS_TERMS, predicate, collector);

    return collector;
  }

  public static Set<String> getAllTermsFromAnnotations(Function<Class<? extends Annotation>, Boolean> predicate) {
    Set<String> collector = new LinkedHashSet<>();
    collectFromAnnotations(CONNECTION_TERMS, predicate, collector);
    collectFromAnnotations(PARAMS_TERMS, predicate, collector);

    return collector;
  }

  private static void collectFromAnnotations(Map<Class<? extends Annotation>, String> terms,
                                             Function<Class<? extends Annotation>, Boolean> predicate,
                                             Set<String> collector) {
    terms.forEach((annotationType, term) -> {
      if (predicate.apply(annotationType)) {
        collector.add(term);
      }
    });
  }

  public static Set<String> getConnectionTerms(Set<String> terms) {
    return extractMatching(CONNECTION_TERMS.values(), terms);
  }

  public static Set<String> getParameterTerms(Set<String> terms) {
    return extractMatching(PARAMS_TERMS.values(), terms);
  }

  private static Set<String> extractMatching(Collection<String> searchSpace, Set<String> subset) {
    if (subset != null) {
      return subset.stream()
          .filter(term -> term.startsWith(CONNECTIVITY_PREFIX) || searchSpace.contains(term))
          .collect(toCollection(LinkedHashSet::new));
    }

    return new LinkedHashSet<>();
  }

  private SemanticTermsHelper() {}
}
