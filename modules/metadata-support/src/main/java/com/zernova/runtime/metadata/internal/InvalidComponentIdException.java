/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metadata.internal;

import static com.zernova.runtime.api.metadata.resolving.FailureCode.UNKNOWN;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.api.metadata.resolving.FailureCode;
import com.zernova.runtime.core.api.ZernovaContext;

/**
 * Represents that the given {@link Location} is invalid due that the Component could not be found in the current
 * {@link ZernovaContext}.
 *
 * @since 4.0
 */
public class InvalidComponentIdException extends ZernovaException {

  private final FailureCode failureCode;

  /**
   * @param message the exception message
   */
  InvalidComponentIdException(I18nMessage message) {
    super(message);
    failureCode = UNKNOWN;
  }

  /**
   * @param message the exception message
   * @param code    {@link FailureCode} associated to the exception
   */
  InvalidComponentIdException(I18nMessage message, FailureCode code) {
    super(message);
    failureCode = code;
  }

  /**
   * @param message the exception message
   * @param cause   the exception that cause this exception to be thrown
   */
  InvalidComponentIdException(I18nMessage message, Throwable cause) {
    super(message, cause);
    failureCode = UNKNOWN;
  }

  /**
   * @param message the exception message
   * @param cause   the exception that cause this exception to be thrown
   * @param code    {@link FailureCode} associated to the exception
   */
  InvalidComponentIdException(I18nMessage message, Throwable cause, FailureCode code) {
    super(message, cause);
    failureCode = code;
  }

  public FailureCode getFailureCode() {
    return failureCode;
  }
}
