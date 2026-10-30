/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.security.adapter;

import com.zernova.sdk.api.security.Credentials;

/**
 *
 *
 * @since 4.5.0
 */
public class SdkCredentialsAdapter implements Credentials {

  private final com.zernova.runtime.api.security.Credentials delegate;

  public SdkCredentialsAdapter(com.zernova.runtime.api.security.Credentials delegate) {
    this.delegate = delegate;
  }

  @Override
  public String getUsername() {
    return delegate.getUsername();
  }

  @Override
  public char[] getPassword() {
    return delegate.getPassword();
  }

  @Override
  public Object getRoles() {
    return delegate.getRoles();
  }
}
