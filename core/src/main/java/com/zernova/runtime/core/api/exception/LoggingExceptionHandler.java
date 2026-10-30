/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.exception;

import static com.zernova.runtime.api.exception.ExceptionHelper.getRootMuleException;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;

import org.slf4j.Logger;

/**
 * {@link FlowExceptionHandler} which can be used to configure a {@link MessageProcessorChain} to not handle errors, only log
 * them. This should be the case when error handling is impossible but errors should not be simply ignored.
 *
 * @since 4.0
 */
@NoExtend
public class LoggingExceptionHandler extends BaseExceptionHandler {

  private static final LoggingExceptionHandler INSTANCE = new LoggingExceptionHandler();
  private static final Logger LOGGER = getLogger(LoggingExceptionHandler.class);

  private LoggingExceptionHandler() {}

  public static FlowExceptionHandler getInstance() {
    return INSTANCE;
  }

  @Override
  protected void onError(Exception exception) {
    ZernovaException me = getRootMuleException(exception);
    if (me != null) {
      LOGGER.error(me.getDetailedMessage());
    } else {
      LOGGER.error("'{}: {}' has occurred.", exception.getClass().getName(), exception.getMessage(), exception);
    }
  }

  @Override
  public String toString() {
    return LoggingExceptionHandler.class.getSimpleName();
  }
}
