/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing.result;

import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.api.i18n.I18nMessage;

/**
 * <code>CouldNotRouteOutboundMessageException</code> thrown if Mule fails to route the current outbound event.
 */

public final class CouldNotRouteOutboundMessageException extends RoutingException {

  /**
   * Serial version
   */
  private static final long serialVersionUID = 4609966704030524485L;

  public CouldNotRouteOutboundMessageException(Processor target) {
    super(target);
  }

  public CouldNotRouteOutboundMessageException(Processor target, Throwable cause) {
    super(target, cause);
  }

  public CouldNotRouteOutboundMessageException(I18nMessage message, Processor target) {
    super(message, target);
  }

  public CouldNotRouteOutboundMessageException(I18nMessage message, Processor target, Throwable cause) {
    super(message, target, cause);
  }
}
