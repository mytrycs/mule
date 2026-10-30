/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.impl.span.command;

import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.tracer.api.context.SpanContextAware;
import com.zernova.runtime.tracer.impl.span.error.DefaultSpanCallStack;
import com.zernova.runtime.tracer.impl.span.error.DefaultSpanError;

import java.util.function.Supplier;

import org.apache.commons.lang3.function.TriFunction;
import org.slf4j.Logger;

/**
 * An {@link AbstractFailSafeVoidTriCommand} that records an error
 *
 * The carrier is the {@link com.zernova.runtime.api.event.EventContext}
 *
 * @since 4.5.0
 */
public class EventContextRecordErrorCommand extends AbstractFailSafeVoidTriCommand<CoreEvent, Supplier<Error>, Boolean> {

  private final TriFunction<CoreEvent, Supplier<Error>, Boolean, Void> triConsumer;

  public static EventContextRecordErrorCommand getEventContextRecordErrorCommand(Logger logger,
                                                                                 String errorMessage,
                                                                                 boolean propagateException) {
    return new EventContextRecordErrorCommand(logger, errorMessage, propagateException);
  }

  public EventContextRecordErrorCommand(Logger logger, String errorMessage, boolean propagateException) {
    super(logger, errorMessage, propagateException);
    this.triConsumer = (coreEvent, spanErrorSupplier, isErrorEscapingCurrentSpan) -> {
      EventContext eventContext = coreEvent.getContext();
      if (eventContext instanceof SpanContextAware) {
        ((SpanContextAware) eventContext)
            .getSpanContext()
            .recordErrorAtSpan(new DefaultSpanError(spanErrorSupplier.get(),
                                                    new DefaultSpanCallStack(coreEvent.getFlowCallStack()),
                                                    isErrorEscapingCurrentSpan));
      }

      return null;
    };
  }


  @Override
  TriFunction<CoreEvent, Supplier<Error>, Boolean, Void> getTriConsumer() {
    return triConsumer;
  }
}
