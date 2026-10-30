/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.security;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.zernova.runtime.api.exception.ZernovaException;

/**
 * {@code SecurityProviderNotFoundException} is thrown by the SecurityManager when an authentication request is made but no
 * suitable security provider can be found to process the authentication.
 *
 * @since 1.0
 */
public final class SecurityProviderNotFoundException extends ZernovaException {

  /**
   * Serial version
   */
  private static final long serialVersionUID = -1730399161002458171L;

  public SecurityProviderNotFoundException(String providerName) {
    super(createStaticMessage("There is no Security Provider registered called %s", providerName));
  }

  public SecurityProviderNotFoundException(String providerName, Throwable cause) {
    super(createStaticMessage("There is no Security Provider registered called %s", providerName), cause);
  }
}
