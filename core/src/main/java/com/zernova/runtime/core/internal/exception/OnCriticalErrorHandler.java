/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.exception;

import static com.zernova.runtime.api.message.error.matcher.ErrorTypeMatcherUtils.createErrorTypeMatcher;
import static com.zernova.runtime.config.internal.error.ZernovaCoreErrorTypeRepository.CRITICAL_ERROR_TYPE;

import static reactor.core.publisher.Mono.error;

import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.message.error.matcher.ErrorTypeMatcher;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.privileged.exception.DefaultExceptionListener;
import com.zernova.runtime.core.privileged.exception.MessagingException;
import com.zernova.runtime.core.privileged.exception.MessagingExceptionHandlerAcceptor;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import jakarta.inject.Inject;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler that only accepts CRITICAL errors, logging them before propagating them. This handler is added before any others in all
 * error handlers.
 *
 * @since 4.0
 */
public class OnCriticalErrorHandler implements MessagingExceptionHandlerAcceptor {

  private static final Logger logger = LoggerFactory.getLogger(OnCriticalErrorHandler.class);

  private final ErrorTypeMatcher criticalMatcher = createErrorTypeMatcher(CRITICAL_ERROR_TYPE);
  private final ErrorTypeMatcher overloadMatcher;

  @Inject
  private NotificationDispatcher notificationFirer;

  private final DefaultExceptionListener exceptionListener;

  public OnCriticalErrorHandler(ErrorTypeMatcher overloadMatcher) {
    this.overloadMatcher = overloadMatcher;
    exceptionListener = new DefaultExceptionListener();
    exceptionListener.setNotificationFirer(notificationFirer);
    exceptionListener.setRepresentation(toString());
  }

  @Override
  public boolean accept(CoreEvent event) {
    Optional<Error> error = event.getError();
    return error.isPresent() && criticalMatcher.match(error.get().getErrorType());
  }

  @Override
  public boolean acceptsAll() {
    return false;
  }

  @Override
  public CoreEvent handleException(Exception exception, CoreEvent event) {
    logException(exception);
    return event;
  }

  @Override
  public Publisher<CoreEvent> apply(Exception exception) {
    logException(exception);
    return error(exception);
  }

  public void logException(Throwable exception) {
    if (exception instanceof MessagingException && ((MessagingException) exception).getEvent().getError().isPresent()) {
      ErrorType errorType = ((MessagingException) exception).getEvent().getError().get().getErrorType();
      if (overloadMatcher.match(errorType)) {
        if (logger.isInfoEnabled()) {
          logger.info(exceptionListener.resolveExceptionAndMessageToLog(exception).toString());
        }
        return;
      }
    }
    exceptionListener.resolveAndLogException(exception);
  }

  @Override
  public Consumer<Exception> router(Function<Publisher<CoreEvent>, Publisher<CoreEvent>> publisherPostProcessor,
                                    Consumer<CoreEvent> continueCallback,
                                    Consumer<Throwable> propagateCallback) {
    return error -> {
      logException(error);
      propagateCallback.accept(error);
    };
  }

  @Override
  public String toString() {
    return "OnCriticalErrorHandler";
  }

  public DefaultExceptionListener getExceptionListener() {
    return exceptionListener;
  }
}
