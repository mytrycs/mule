/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import com.zernova.runtime.core.internal.exception.ErrorHandler;
import com.zernova.runtime.core.internal.exception.GlobalErrorHandler;
import com.zernova.runtime.core.privileged.exception.MessagingExceptionHandlerAcceptor;
import com.zernova.runtime.dsl.api.component.AbstractComponentFactory;

import java.util.List;

/**
 * An {@link com.zernova.runtime.dsl.api.component.ObjectFactory} which produces {@link ErrorHandler} instances.
 *
 * @since 4.1.0
 */
public class ErrorHandlerFactoryBean extends AbstractComponentFactory<ErrorHandler> {

  private GlobalErrorHandler delegate;
  private List<MessagingExceptionHandlerAcceptor> exceptionListeners;
  private String name;

  @Override
  public ErrorHandler doGetObject() throws Exception {
    if (delegate != null) {
      return delegate;
    }

    ErrorHandler errorHandler;
    if (isGlobalErrorHandler()) {
      errorHandler = new GlobalErrorHandler();
      errorHandler.setName(name);
      errorHandler.setExceptionListeners(exceptionListeners);
      ((GlobalErrorHandler) errorHandler).setFromGlobalErrorHandler();
    } else {
      errorHandler = new ErrorHandler();
      errorHandler.setExceptionListeners(exceptionListeners);
    }
    return errorHandler;
  }

  private boolean isGlobalErrorHandler() {
    return getLocation().getParts().size() == 1;
  }

  public void setDelegate(GlobalErrorHandler delegate) {
    this.delegate = delegate;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setExceptionListeners(List<MessagingExceptionHandlerAcceptor> exceptionListeners) {
    this.exceptionListeners = exceptionListeners;
  }

}
