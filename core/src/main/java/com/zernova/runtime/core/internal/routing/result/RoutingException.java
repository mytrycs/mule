/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing.result;

import static com.zernova.runtime.core.api.config.i18n.CoreMessages.failedToRouterViaEndpoint;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.core.api.processor.Processor;

/**
 * <code>RoutingException</code> is a base class for all routing exceptions. Routing exceptions are only thrown for
 * DefaultInboundRouterCollection and DefaultOutboundRouterCollection and deriving types. Mule itself does not throw routing
 * exceptions when routing internal events.
 */
public class RoutingException extends ZernovaException {

  /**
   * Serial version
   */
  private static final long serialVersionUID = 2478458847072048647L;

  protected final transient Processor route;

  public RoutingException(Processor route) {
    super(generateMessage(null, route));
    this.route = route;
  }

  public RoutingException(Processor route, Throwable cause) {
    super(generateMessage(null, route), cause);
    this.route = route;
  }

  public RoutingException(I18nMessage message, Processor route) {
    super(generateMessage(message, route));
    this.route = route;
  }

  public RoutingException(I18nMessage message, Processor route, Throwable cause) {
    super(generateMessage(message, route), cause);
    this.route = route;
  }

  public Processor getRoute() {
    return route;
  }

  private static I18nMessage generateMessage(I18nMessage message, Processor target) {
    I18nMessage m = failedToRouterViaEndpoint(target);
    if (message != null) {
      message.setNextMessage(m);
      return message;
    } else {
      return m;
    }
  }
}
