/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.api.server;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import com.zernova.runtime.api.exception.ZernovaException;

/**
 * Exception thrown when a server cannot be created.
 *
 * @since 4.0
 */
public class ServerCreationException extends ZernovaException {

  private static final long serialVersionUID = -7954287390178516550L;

  public ServerCreationException(String message, Throwable cause) {
    super(createStaticMessage("Could not create server: " + message), cause);
  }

  public ServerCreationException(String message) {
    super(createStaticMessage("Could not create server: " + message));
  }

}
