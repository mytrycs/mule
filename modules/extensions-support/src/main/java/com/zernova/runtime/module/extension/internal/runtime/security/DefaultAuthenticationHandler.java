/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.security;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import com.zernova.runtime.api.security.Authentication;
import com.zernova.runtime.api.security.Credentials;
import com.zernova.runtime.api.security.CredentialsBuilder;
import com.zernova.runtime.api.security.DefaultZernovaAuthentication;
import com.zernova.runtime.api.security.SecurityContext;
import com.zernova.runtime.api.security.SecurityException;
import com.zernova.runtime.api.security.SecurityProviderNotFoundException;
import com.zernova.runtime.api.security.UnknownAuthenticationTypeException;
import com.zernova.runtime.core.api.security.SecurityManager;
import com.zernova.runtime.core.api.security.SecurityProvider;
import com.zernova.runtime.core.internal.security.DefaultZernovaSecurityManager;
import com.zernova.runtime.extension.api.security.AuthenticationHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Default implementation of a {@link AuthenticationHandler}
 *
 * @since 4.0
 */
public class DefaultAuthenticationHandler implements AuthenticationHandler {

  private SecurityContext securityContext;
  private SecurityManager manager;
  private final Consumer<SecurityContext> securityContextUpdater;

  public DefaultAuthenticationHandler(SecurityContext securityContext, SecurityManager manager,
                                      Consumer<SecurityContext> securityContextUpdater) {
    this.securityContext = securityContext;
    this.manager = manager;
    this.securityContextUpdater = securityContextUpdater;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAuthentication(Authentication authentication)
      throws SecurityProviderNotFoundException, SecurityException, UnknownAuthenticationTypeException {

    Authentication authResult = manager.authenticate(authentication);

    if (securityContext == null) {
      securityContext = manager.createSecurityContext(authResult);
    }

    this.securityContext.setAuthentication(authResult);
    this.securityContextUpdater.accept(securityContext);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAuthentication(List<String> securityProviders, Authentication authentication)
      throws SecurityProviderNotFoundException, SecurityException, UnknownAuthenticationTypeException {
    if (!securityProviders.isEmpty()) {
      final List<SecurityProvider> providers = new ArrayList<>();

      // This filter may only allow authentication on a subset of registered security providers
      for (String sp : securityProviders) {
        SecurityProvider provider = manager.getProvider(sp);
        if (provider != null) {
          providers.add(provider);
        } else {
          throw new SecurityProviderNotFoundException(sp);
        }
      }
      this.manager = new DefaultZernovaSecurityManager(providers);
    }

    setAuthentication(authentication);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<Authentication> getAuthentication() {
    return securityContext != null ? ofNullable(securityContext.getAuthentication()) : empty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Authentication createAuthentication(Credentials credentials) {
    return new DefaultZernovaAuthentication(credentials);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CredentialsBuilder createCredentialsBuilder() {
    return new DefaultCredentialsBuilder();
  }

}
