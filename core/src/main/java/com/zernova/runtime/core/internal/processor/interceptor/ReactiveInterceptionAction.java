/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor.interceptor;

import static com.zernova.runtime.core.internal.exception.InternalExceptionUtils.getErrorFromFailingProcessor;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.WITHIN_PROCESS_TO_APPLY;

import static java.util.concurrent.CompletableFuture.completedFuture;

import static org.slf4j.LoggerFactory.getLogger;
import static reactor.core.publisher.Mono.just;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.interception.InterceptionAction;
import com.zernova.runtime.api.interception.InterceptionEvent;
import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.ReactiveProcessor;
import com.zernova.runtime.core.internal.event.InternalEvent;
import com.zernova.runtime.core.internal.interception.DefaultInterceptionEvent;
import com.zernova.runtime.core.privileged.exception.ErrorTypeLocator;
import com.zernova.runtime.core.privileged.exception.MessagingException;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;

import reactor.util.context.ContextView;

/**
 * Implementation of {@link InterceptionAction} that does the needed hooks with {@code Reactor} into the pipeline.
 *
 * @since 4.0
 */
class ReactiveInterceptionAction implements InterceptionAction {

  private static final Logger LOGGER = getLogger(ReactiveInterceptionAction.class);

  private final ErrorTypeLocator errorTypeLocator;

  private final ReactiveProcessor processor;
  private final ReactiveProcessor next;
  private final ContextView ctx;
  private final DefaultInterceptionEvent interceptionEvent;

  public ReactiveInterceptionAction(DefaultInterceptionEvent interceptionEvent,
                                    ReactiveProcessor next, ContextView ctx, ReactiveProcessor processor,
                                    ErrorTypeLocator errorTypeLocator) {
    this.interceptionEvent = interceptionEvent;
    this.next = next;
    this.ctx = ctx;
    this.processor = processor;
    this.errorTypeLocator = errorTypeLocator;
  }

  @Override
  public CompletableFuture<InterceptionEvent> proceed() {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Called proceed() for processor {}", ((Component) processor).getLocation().getLocation());
    }

    return just(interceptionEvent.resolve())
        .cast(CoreEvent.class)
        .transform(next)
        .cast(InternalEvent.class)
        .map(interceptionEvent::reset)
        .cast(InterceptionEvent.class)
        // This is needed for all cases because the invoked component may use fluxes
        .contextWrite(innerCtx -> innerCtx.put(WITHIN_PROCESS_TO_APPLY, true))
        .contextWrite(ctx)
        .toFuture();
  }

  @Override
  public CompletableFuture<InterceptionEvent> skip() {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Called skip() for processor {}", ((Component) processor).getLocation().getLocation());
    }

    interceptionEvent.resolve();
    return completedFuture(interceptionEvent);
  }

  @Override
  public CompletableFuture<InterceptionEvent> fail(Throwable cause) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Called fail() for processor {} with cause {} ({})", ((Component) processor).getLocation().getLocation(),
                   cause.getClass(), cause.getMessage());
    }

    Error newError = getErrorFromFailingProcessor(null, (Component) processor, cause, errorTypeLocator);

    interceptionEvent.setError(newError.getErrorType(), cause);
    CompletableFuture<InterceptionEvent> completableFuture = new CompletableFuture<>();
    completableFuture
        .completeExceptionally(new MessagingException(interceptionEvent.resolve(), cause, (Component) processor));
    return completableFuture;
  }

  private CompletableFuture<InterceptionEvent> failWithMessage(ErrorType errorType, String msg) {
    Throwable cause = new InterceptionException(msg);
    interceptionEvent.setError(errorType, cause);
    CompletableFuture<InterceptionEvent> completableFuture = new CompletableFuture<>();
    completableFuture.completeExceptionally(new MessagingException(interceptionEvent.resolve(), cause, (Component) processor));
    return completableFuture;
  }

  @Override
  public CompletableFuture<InterceptionEvent> fail(ErrorType errorType) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Called fail() for processor {} with errorType {}", ((Component) processor).getLocation().getLocation(),
                   errorType.getIdentifier());
    }
    return failWithMessage(errorType, "");
  }

  @Override
  public CompletableFuture<InterceptionEvent> fail(ErrorType errorType, String msg) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Called fail() for processor {} with errorType {} and message {}",
                   ((Component) processor).getLocation().getLocation(), errorType.getIdentifier(), msg);
    }
    return failWithMessage(errorType, msg);
  }
}
