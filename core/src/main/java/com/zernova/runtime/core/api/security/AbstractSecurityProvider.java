/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.security;

import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.security.Authentication;
import com.zernova.runtime.api.security.SecurityContext;
import com.zernova.runtime.api.security.UnknownAuthenticationTypeException;
import com.zernova.runtime.core.internal.security.DefaultSecurityContextFactory;

public abstract class AbstractSecurityProvider extends AbstractComponent implements SecurityProvider, Initialisable {

  private String name;
  private SecurityContextFactory securityContextFactory;

  public AbstractSecurityProvider(String name) {
    this.name = name;
  }

  @Override
  public final void initialise() throws InitialisationException {
    doInitialise();

    if (securityContextFactory == null) {
      securityContextFactory = new DefaultSecurityContextFactory();
    }
  }

  protected void doInitialise() throws InitialisationException {
    // do nothing by default
  }

  @Override
  public boolean supports(Class<?> aClass) {
    return Authentication.class.isAssignableFrom(aClass);
  }

  @Override
  public SecurityContext createSecurityContext(Authentication authentication) throws UnknownAuthenticationTypeException {
    return securityContextFactory.create(authentication);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public SecurityContextFactory getSecurityContextFactory() {
    return securityContextFactory;
  }

  public void setSecurityContextFactory(SecurityContextFactory securityContextFactory) {
    this.securityContextFactory = securityContextFactory;
  }

}
