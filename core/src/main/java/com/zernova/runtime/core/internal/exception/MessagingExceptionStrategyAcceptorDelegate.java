/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.exception;

import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.exception.FlowExceptionHandler;
import com.zernova.runtime.core.api.processor.AbstractZernovaObjectOwner;
import com.zernova.runtime.core.privileged.exception.MessagingExceptionHandlerAcceptor;

import org.reactivestreams.Publisher;

import java.util.Arrays;
import java.util.List;

/**
 * Allows to use {@link com.zernova.runtime.core.api.exception.FlowExceptionHandler} as {@link MessagingExceptionHandlerAcceptor}.
 */
public class MessagingExceptionStrategyAcceptorDelegate extends AbstractZernovaObjectOwner<FlowExceptionHandler>
    implements MessagingExceptionHandlerAcceptor {

  private FlowExceptionHandler delegate;

  public MessagingExceptionStrategyAcceptorDelegate(FlowExceptionHandler messagingExceptionHandler) {
    this.delegate = messagingExceptionHandler;
  }

  @Override
  public boolean accept(CoreEvent event) {
    if (delegate instanceof MessagingExceptionHandlerAcceptor) {
      return ((MessagingExceptionHandlerAcceptor) delegate).accept(event);
    }
    return true;
  }

  @Override
  public boolean acceptsAll() {
    if (delegate instanceof MessagingExceptionHandlerAcceptor) {
      return ((MessagingExceptionHandlerAcceptor) delegate).acceptsAll();
    }
    return true;
  }

  @Override
  public CoreEvent handleException(Exception exception, CoreEvent event) {
    return delegate.handleException(exception, event);
  }

  @Override
  public Publisher<CoreEvent> apply(Exception exception) {
    return delegate.apply(exception);
  }

  @Override
  protected List<FlowExceptionHandler> getOwnedObjects() {
    return Arrays.asList(delegate);
  }

  public FlowExceptionHandler getExceptionListener() {
    return this.delegate;
  }
}
