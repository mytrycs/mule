/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.processor;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.event.EventContextFactory.create;
import static com.zernova.runtime.core.internal.event.DefaultEventContext.child;
import static com.zernova.runtime.core.internal.event.EventQuickCopy.quickCopy;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import static reactor.core.publisher.Mono.from;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.component.execution.ComponentExecutionException;
import com.zernova.runtime.api.component.execution.ExecutableComponent;
import com.zernova.runtime.api.component.execution.ExecutionResult;
import com.zernova.runtime.api.component.execution.InputEvent;
import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.event.CoreEvent.Builder;
import com.zernova.runtime.core.api.processor.ReactiveProcessor;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import com.zernova.runtime.core.privileged.exception.MessagingException;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import jakarta.inject.Inject;

import reactor.core.publisher.Mono;

/**
 * Abstract implementation of {@link ExecutableComponent}.
 *
 * @since 4.0
 */
@NoImplement
public abstract class AbstractExecutableComponent extends AbstractComponent implements ExecutableComponent {

  @Inject
  protected ZernovaContext muleContext;

  @Override
  public final CompletableFuture<ExecutionResult> execute(InputEvent inputEvent) {
    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    CoreEvent.Builder builder = CoreEvent.builder(createEventContext(of(completableFuture)));
    CoreEvent event = builder.message(inputEvent.getMessage())
        .error(inputEvent.getError().orElse(null))
        .variables(inputEvent.getVariables())
        .build();
    return doProcess(event)
        .doOnError(e -> completableFuture.complete(null))
        .<ExecutionResult>map(result -> new ExecutionResultImplementation(result, completableFuture))
        .toFuture();
  }

  @Override
  public final CompletableFuture<Event> execute(Event event) {
    CoreEvent internalEvent = null;
    if (event instanceof CoreEvent) {
      BaseEventContext child = createChildEventContext(event.getContext());
      internalEvent = quickCopy(child, (CoreEvent) event);
    } else {
      internalEvent = CoreEvent.builder(createEventContext(empty()))
          .message(event.getMessage())
          .error(event.getError().orElse(null))
          .variables(event.getVariables())
          .build();
    }

    final CompletableFuture<Event> future = doProcess(internalEvent)
        .map(r -> quickCopy(event.getContext(), r))
        .cast(Event.class)
        .toFuture();
    return withCallbacks(future, internalEvent.getContext());
  }

  /**
   * Executes the component based on a {@link Event} that may have been provided by another component execution.
   * <p>
   * Streams will be closed and resources cleaned up when when the existing root {@link com.zernova.runtime.api.event.EventContext}
   * completes.
   *
   * @param event                 the input to execute the component
   * @param childEventContributor allows to perform any modifications on the event to be built right before executing this
   *                              component with it.
   * @return a {@link Event} with the content of the result
   * @throws ComponentExecutionException if there is an unhandled error within the execution
   */
  public final CompletableFuture<Event> execute(Event event, Consumer<CoreEvent.Builder> childEventContributor) {
    EventContext child = null;
    if (event instanceof CoreEvent) {
      child = createChildEventContext(event.getContext());
    } else {
      child = createEventContext(empty());
    }

    final Builder internalEventBuilder = CoreEvent.builder(child)
        .message(event.getMessage())
        .error(event.getError().orElse(null))
        .variables(event.getVariables());
    childEventContributor.accept(internalEventBuilder);

    final CompletableFuture<Event> future = doProcess(internalEventBuilder.build())
        .map(r -> quickCopy(event.getContext(), r))
        .cast(Event.class)
        .toFuture();
    return withCallbacks(future, child);
  }

  private CompletableFuture<Event> withCallbacks(CompletableFuture<Event> future, EventContext context) {
    future.whenComplete((e, t) -> {
      if (t != null) {
        ((BaseEventContext) context).error(t);
      } else {
        ((BaseEventContext) context).success();
      }
    });

    return future;
  }

  private EventContext createEventContext(Optional<CompletableFuture<Void>> externalCompletion) {
    return create(muleContext.getUniqueIdString(), muleContext.getId(), getLocation(), null, externalCompletion);
  }

  private BaseEventContext createChildEventContext(EventContext parent) {
    return child((BaseEventContext) parent, ofNullable(getLocation()));
  }

  private Mono<CoreEvent> doProcess(CoreEvent event) {
    return from(MessageProcessors.process(event, getExecutableFunction()))
        .onErrorMap(throwable -> {
          CoreEvent messagingExceptionEvent = ((MessagingException) throwable).getEvent();
          return new ComponentExecutionException(messagingExceptionEvent.getError()
              .map(t -> t.getCause())
              .orElse(throwable.getCause()),
                                                 ((BaseEventContext) messagingExceptionEvent.getContext()).getParentContext()
                                                     .map(pc -> quickCopy(pc, messagingExceptionEvent))
                                                     .orElse(messagingExceptionEvent));
        });
  }

  /**
   * Template method that allows to return a function to execute for this component. It may not be redefine by implementation of
   * this class if they are already instances of {@link Function< Publisher<CoreEvent>, Publisher<InternalEvent>>}
   *
   * @return an executable function. It must not be null.
   */
  protected ReactiveProcessor getExecutableFunction() {
    if (this instanceof ReactiveProcessor) {
      return (ReactiveProcessor) this;
    }
    throw new ZernovaRuntimeException(createStaticMessage(format("Method getExecutableFunction not redefined and instance %s is not of type ReactiveProcessor",
                                                              this)));
  }

  public void setMuleContext(ZernovaContext muleContext) {
    this.muleContext = muleContext;
  }

  private static class ExecutionResultImplementation implements ExecutionResult {

    private final Event result;
    private final CompletableFuture<Void> complete;

    private ExecutionResultImplementation(Event result, CompletableFuture<Void> complete) {
      this.result = result;
      this.complete = complete;
    }

    @Override
    public Event getEvent() {
      return result;
    }

    @Override
    public void complete() {
      complete.complete(null);
    }
  }

}
