/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.impl.span.command;

import static com.zernova.runtime.tracer.impl.context.EventSpanContext.builder;

import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.tracer.api.context.SpanContextAware;
import com.zernova.runtime.tracer.api.context.getter.DistributedTraceContextGetter;

import java.util.function.BiConsumer;

import org.slf4j.Logger;

/**
 * An {@link AbstractFailSafeVoidBiCommand} that injects the span context.
 *
 * The carrier is the {@link com.zernova.runtime.api.event.EventContext}
 *
 * @since 4.5.0
 */
public class EventContextInjectDistributedTraceContextCommand
    extends AbstractFailSafeVoidBiCommand<EventContext, DistributedTraceContextGetter> {

  private BiConsumer<EventContext, DistributedTraceContextGetter> consumer;

  public static EventContextInjectDistributedTraceContextCommand getEventContextInjectDistributedTraceContextCommand(Logger logger,
                                                                                                                     String errorMessage,
                                                                                                                     boolean propagateExceptions) {
    return new EventContextInjectDistributedTraceContextCommand(logger, errorMessage, propagateExceptions);
  }

  private EventContextInjectDistributedTraceContextCommand(Logger logger, String errorMessage, boolean propagateExceptions) {
    super(logger, errorMessage, propagateExceptions);
    this.consumer = (eventContext, getter) -> {
      if (eventContext instanceof SpanContextAware) {
        ((SpanContextAware) eventContext).setSpanContext(
                                                         builder()
                                                             .withGetter(getter)
                                                             .withManagedChildSpan(true)
                                                             .build());
      }
    };
  }

  @Override
  BiConsumer<EventContext, DistributedTraceContextGetter> getConsumer() {
    return consumer;
  }
}
