/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.security.adapter;

import com.zernova.runtime.api.security.Authentication;

import java.util.Map;

/**
 * Adapts a {@link com.zernova.sdk.api.security.Authentication} into a {@link Authentication}
 *
 * @since 4.5.0
 */
public class ZernovaAuthenticationAdapter implements Authentication {

  private final com.zernova.sdk.api.security.Authentication delegate;

  public ZernovaAuthenticationAdapter(com.zernova.sdk.api.security.Authentication delegate) {
    this.delegate = delegate;
  }

  @Override
  public Object getCredentials() {
    return delegate.getCredentials();
  }

  @Override
  public Object getPrincipal() {
    return delegate.getPrincipal();
  }

  @Override
  public Map<String, Object> getProperties() {
    return delegate.getProperties();
  }

  @Override
  public Authentication setProperties(Map<String, Object> properties) {
    delegate.setProperties(properties);
    return this;
  }
}
