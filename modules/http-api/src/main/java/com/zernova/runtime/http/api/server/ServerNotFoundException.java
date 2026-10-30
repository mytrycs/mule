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
 * Exception thrown when a reference to a server fails.
 *
 * @since 4.0
 */
public final class ServerNotFoundException extends ZernovaException {

  private static final long serialVersionUID = 2587466114314625851L;

  public ServerNotFoundException(String serverName) {
    super(createStaticMessage("Server '%s' could not be found.", serverName));
  }

}
