/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.security;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import com.zernova.runtime.api.exception.ZernovaException;

/**
 * {@code EncryptionStrategyNotFoundException} thrown by the {@link java.lang.SecurityManager} when an encryption scheme is set in
 * a property or header that has not been registered with the manager.
 *
 * @since 4.0
 */
public final class EncryptionStrategyNotFoundException extends ZernovaException {

  /**
   * Serial version
   */
  private static final long serialVersionUID = 3916371211189075141L;

  public EncryptionStrategyNotFoundException(String strategyName) {
    super(createStaticMessage("There is no Encryption Strategy registered called '%s'", strategyName));
  }

  public EncryptionStrategyNotFoundException(String strategyName, Throwable cause) {
    super(createStaticMessage("There is no Encryption Strategy registered called '%s'", strategyName), cause);
  }
}
