/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.i18n.I18nMessage;

/**
 * Exception through by routing strategies when routing fails
 */
public class RoutingFailedException extends ZernovaException {

  public RoutingFailedException(I18nMessage message) {
    super(message);
  }

  public RoutingFailedException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }

  public RoutingFailedException(Throwable cause) {
    super(cause);
  }

}
