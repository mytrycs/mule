/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.impl.span.command.spancontext;

import static com.zernova.runtime.tracer.api.context.SpanContext.emptySpanContext;
import static com.zernova.runtime.tracer.api.context.getter.DistributedTraceContextGetter.emptyTraceContextMapGetter;

import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.tracer.api.context.SpanContext;
import com.zernova.runtime.tracer.api.context.SpanContextAware;
import com.zernova.runtime.tracer.impl.context.EventSpanContext;

/**
 * A {@link SpanContextContextGetter} that gets the {@link SpanContext} from the {@link EventContext}.
 *
 * @since 4.5.0
 */
public class SpanContextFromEventContextGetter implements SpanContextContextGetter<EventContext> {

  private static final SpanContextContextGetter<EventContext> INSTANCE = new SpanContextFromEventContextGetter();

  public static SpanContextContextGetter<EventContext> getSpanContextFromEventContextGetter() {
    return INSTANCE;
  }

  private SpanContextFromEventContextGetter() {}

  @Override
  public SpanContext get(EventContext carrier) {
    if (carrier instanceof SpanContextAware) {
      SpanContextAware spanContextAwareCarrier = (SpanContextAware) carrier;
      SpanContext spanContext = spanContextAwareCarrier.getSpanContext();
      if (spanContext == null) {
        spanContext = EventSpanContext
            .builder()
            .withGetter(emptyTraceContextMapGetter())
            .build();
        spanContextAwareCarrier.setSpanContext(spanContext);
      }
      return spanContext;
    }

    return emptySpanContext();
  }
}
