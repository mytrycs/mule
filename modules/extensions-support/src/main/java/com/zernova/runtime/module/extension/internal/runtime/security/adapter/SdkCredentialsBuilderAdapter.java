/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.security.adapter;


import com.zernova.sdk.api.security.Credentials;
import com.zernova.sdk.api.security.CredentialsBuilder;

/**
 * Adapts a {@link com.zernova.runtime.api.security.CredentialsBuilder} into a {@link CredentialsBuilder}
 *
 * @since 4.5.0
 */
public class SdkCredentialsBuilderAdapter implements CredentialsBuilder {

  private final com.zernova.runtime.api.security.CredentialsBuilder delegate;

  public SdkCredentialsBuilderAdapter(com.zernova.runtime.api.security.CredentialsBuilder delegate) {
    this.delegate = delegate;
  }

  @Override
  public CredentialsBuilder withUsername(String username) {
    delegate.withUsername(username);
    return this;
  }

  @Override
  public CredentialsBuilder withPassword(char[] password) {
    delegate.withPassword(password);
    return this;
  }

  @Override
  public CredentialsBuilder withRoles(Object roles) {
    delegate.withRoles(roles);
    return this;
  }

  @Override
  public Credentials build() {
    return new SdkCredentialsAdapter(delegate.build());
  }
}
