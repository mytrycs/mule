/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.security.adapter;

import com.zernova.runtime.api.security.SecurityException;
import com.zernova.runtime.api.security.SecurityProviderNotFoundException;
import com.zernova.runtime.api.security.UnknownAuthenticationTypeException;
import com.zernova.runtime.module.extension.internal.runtime.security.DefaultCredentialsBuilder;
import com.zernova.sdk.api.security.Authentication;
import com.zernova.sdk.api.security.AuthenticationHandler;
import com.zernova.sdk.api.security.Credentials;
import com.zernova.sdk.api.security.CredentialsBuilder;

import java.util.List;
import java.util.Optional;

/**
 * Adapts a legacy {@link com.zernova.runtime.extension.api.security.AuthenticationHandler} into a {@link AuthenticationHandler}
 *
 * @since 4.5.0
 */
public class SdkAuthenticationHandlerAdapter implements AuthenticationHandler {

  private final com.zernova.runtime.extension.api.security.AuthenticationHandler delegate;

  public SdkAuthenticationHandlerAdapter(com.zernova.runtime.extension.api.security.AuthenticationHandler delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setAuthentication(Authentication authentication)
      throws SecurityProviderNotFoundException, SecurityException, UnknownAuthenticationTypeException {
    delegate.setAuthentication(new ZernovaAuthenticationAdapter(authentication));
  }

  @Override
  public void setAuthentication(List<String> list, Authentication authentication)
      throws SecurityProviderNotFoundException, SecurityException, UnknownAuthenticationTypeException {
    delegate.setAuthentication(list, new ZernovaAuthenticationAdapter(authentication));
  }

  @Override
  public Optional<Authentication> getAuthentication() {
    return delegate.getAuthentication().map(SdkAuthenticationAdapter::new);
  }

  @Override
  public Authentication createAuthentication(Credentials credentials) {
    return new SdkAuthenticationAdapter(delegate.createAuthentication(new ZernovaCredentialsAdapter(credentials)));
  }

  @Override
  public CredentialsBuilder createCredentialsBuilder() {
    return new SdkCredentialsBuilderAdapter(delegate.createCredentialsBuilder());
  }
}
