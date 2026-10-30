/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.component.execution;

import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;

/**
 * Exception thrown by {@link ExecutableComponent} when there was a unhandled error within the execution of a set of components.
 *
 * @since 1.0
 */
public final class ComponentExecutionException extends ZernovaRuntimeException {

  private static final long serialVersionUID = 8346554813024241753L;

  private final Event event;

  /**
   * @param cause the exception thrown by the failing component.
   * @param event the {@link Event} at the moment of the exception.
   */
  public ComponentExecutionException(Throwable cause, Event event) {
    super(cause);
    this.event = event;
  }

  /**
   * Provides the {@link Event} associated to the exception, including the resulting {@link com.zernova.runtime.api.message.Error}
   * with all the resolved data, such as it's {@link com.zernova.runtime.api.message.ErrorType} and cause.
   *
   * @return the {@link Event} at the moment of the exception.
   */
  public Event getEvent() {
    return event;
  }
}
