/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.troubleshooting.api;

import com.zernova.api.annotation.Experimental;

/**
 * Exception used to the troubleshooting operations related errors.
 *
 * @since 4.5
 */
@Experimental
public class TroubleshootingOperationException extends Exception {

  private static final long serialVersionUID = -6535704838521318202L;

  public TroubleshootingOperationException(String message, Exception cause) {
    super(message, cause);
  }

  public TroubleshootingOperationException(String mesage) {
    this(mesage, null);
  }
}
