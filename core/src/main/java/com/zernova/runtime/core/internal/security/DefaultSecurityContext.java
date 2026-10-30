/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.security;

import com.zernova.runtime.api.security.Authentication;
import com.zernova.runtime.api.security.SecurityContext;

/**
 * Trivial {@link SecurityContext} implementation which simply holds the {@link Authentication} object.
 */
public class DefaultSecurityContext implements SecurityContext {

  private static final long serialVersionUID = -3209120471953147538L;

  private Authentication authentication;

  public DefaultSecurityContext(Authentication authentication) {
    this.authentication = authentication;
  }

  public final Authentication getAuthentication() {
    return authentication;
  }

  public final void setAuthentication(Authentication authentication) {
    this.authentication = authentication;
  }
}
