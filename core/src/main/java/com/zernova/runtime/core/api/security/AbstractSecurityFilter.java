/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.security;

import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_SECURITY_MANAGER;
import static com.zernova.runtime.core.api.config.i18n.CoreMessages.authSecurityManagerNotSet;
import static com.zernova.runtime.core.api.config.i18n.CoreMessages.objectNotRegistered;
import static com.zernova.runtime.core.api.util.StringUtils.splitAndTrim;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.security.SecurityContext;
import com.zernova.runtime.api.security.SecurityException;
import com.zernova.runtime.api.security.SecurityProviderNotFoundException;
import com.zernova.runtime.api.security.UnknownAuthenticationTypeException;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.security.DefaultZernovaSecurityManager;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>AbstractSecurityFilter</code> provides basic initialisation for all security filters, namely configuring the
 * SecurityManager for this instance
 */
public abstract class AbstractSecurityFilter extends AbstractComponent implements SecurityFilter {

  protected transient Logger logger = LoggerFactory.getLogger(getClass());

  protected SecurityManager securityManager;
  @Inject
  protected Registry registry;

  private String securityProviders;

  @Override
  public final void initialise() throws InitialisationException {
    if (securityManager == null) {
      securityManager = (SecurityManager) registry.lookupByName(OBJECT_SECURITY_MANAGER).orElse(null);
    }

    if (securityManager == null) {
      throw new InitialisationException(authSecurityManagerNotSet(), this);
    }

    // This filter may only allow authentication on a subset of registered
    // security providers
    if (securityProviders != null) {
      SecurityManager localManager = new DefaultZernovaSecurityManager();
      String[] securityProviders = splitAndTrim(this.securityProviders, ",");
      for (String sp : securityProviders) {
        SecurityProvider provider = securityManager.getProvider(sp);
        if (provider != null) {
          localManager.addProvider(provider);
        } else {
          throw new InitialisationException(objectNotRegistered("Security Provider", sp), this);
        }
      }
      securityManager = localManager;
    }

    doInitialise();
  }

  protected void doInitialise() throws InitialisationException {}

  @Override
  public SecurityManager getSecurityManager() {
    return securityManager;
  }

  @Override
  public String getSecurityProviders() {
    return securityProviders;
  }

  @Override
  public abstract SecurityContext doFilter(CoreEvent event) throws SecurityException, UnknownAuthenticationTypeException,
      CryptoFailureException, SecurityProviderNotFoundException, EncryptionStrategyNotFoundException, InitialisationException;

}
