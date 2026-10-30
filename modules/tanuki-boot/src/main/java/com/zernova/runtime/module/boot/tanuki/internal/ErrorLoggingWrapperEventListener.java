/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.boot.tanuki.internal;

import static org.tanukisoftware.wrapper.WrapperManager.WRAPPER_LOG_LEVEL_ERROR;
import static org.tanukisoftware.wrapper.WrapperManager.log;

import org.tanukisoftware.wrapper.event.WrapperEvent;
import org.tanukisoftware.wrapper.event.WrapperEventListener;

/**
 * A {@link WrapperEventListener} used to catch logging events and request to log a message to the native wrapper.
 *
 * @since 4.5
 * @see ZernovaContainerTanukiWrapper#haltAndCatchFire(int, String)
 */
class ErrorLoggingWrapperEventListener implements WrapperEventListener {

  private final String message;
  private boolean errorAlreadyLogged = false;

  ErrorLoggingWrapperEventListener(String message) {
    this.message = message;
  }

  @Override
  public void fired(WrapperEvent event) {
    if (!errorAlreadyLogged && message != null) {
      log(WRAPPER_LOG_LEVEL_ERROR, message);
      errorAlreadyLogged = true;
    }
  }
}
