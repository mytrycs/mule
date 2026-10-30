/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.event;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;

/**
 * Indicates that there are many nested event contexts.
 * <p>
 * This would be an equivalent of the {@link StackOverflowError} for Java in Mule.
 *
 * @since 4.2.0
 */
public class EventContextDeepNestingException extends ZernovaRuntimeException {

  private static final long serialVersionUID = 9171026572161838000L;

  public EventContextDeepNestingException(String message) {
    super(createStaticMessage(message));
  }

}
