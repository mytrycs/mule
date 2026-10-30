/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.transaction.xa;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.i18n.I18nMessage;

public class ResourceManagerException extends ZernovaException {

  /**
   * Serial version
   */
  private static final long serialVersionUID = -2710661653314559258L;

  /**
   *
   */
  public ResourceManagerException() {
    super();
  }

  /**
   * @param message
   */
  public ResourceManagerException(I18nMessage message) {
    super(message);
  }

  /**
   * @param cause
   */
  public ResourceManagerException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public ResourceManagerException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }

}
