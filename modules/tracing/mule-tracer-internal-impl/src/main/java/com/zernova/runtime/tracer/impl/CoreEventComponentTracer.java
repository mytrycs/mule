/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.impl;

import com.zernova.runtime.api.profiling.tracing.Span;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.tracer.api.EventTracer;
import com.zernova.runtime.tracer.api.component.ComponentTracer;
import com.zernova.runtime.tracer.api.span.info.InitialSpanInfo;
import com.zernova.runtime.tracer.api.span.validation.Assertion;

import com.zernova.runtime.core.internal.profiling.tracing.event.span.condition.SpanNameAssertion;

import java.util.Optional;

import static com.zernova.runtime.core.internal.profiling.tracing.event.span.condition.NotNullSpanAssertion.getNotNullSpanTracingCondition;

/**
 * Default implementation of a {@link ComponentTracer}. It uses a {@link CoreEvent} as the
 * {@link com.zernova.runtime.tracer.api.context.SpanContext} carrier and a {@link CoreEventTracer} as a delegate. It also performs
 * standard validations both on start and on ending the spans associated with the
 * {@link com.zernova.runtime.api.component.Component}.
 *
 * @since 4.5.0
 */
public class CoreEventComponentTracer implements ComponentTracer<CoreEvent> {

  private final InitialSpanInfo initialSpanInfo;
  private final EventTracer<CoreEvent> coreEventTracer;
  private final Assertion parentComponentSpanAssertion;
  private final Assertion componentSpanAssertion;

  public CoreEventComponentTracer(InitialSpanInfo initialSpanInfo, EventTracer<CoreEvent> coreEventTracer) {
    this.initialSpanInfo = initialSpanInfo;
    this.coreEventTracer = coreEventTracer;
    this.parentComponentSpanAssertion = getNotNullSpanTracingCondition();
    this.componentSpanAssertion = new SpanNameAssertion(initialSpanInfo.getName());
  }

  public CoreEventComponentTracer(InitialSpanInfo initialSpanInfo, EventTracer<CoreEvent> coreEventTracer,
                                  ComponentTracer<?> parentComponentTracer) {
    this.initialSpanInfo = initialSpanInfo;
    this.coreEventTracer = coreEventTracer;
    this.componentSpanAssertion = new SpanNameAssertion(initialSpanInfo.getName());
    this.parentComponentSpanAssertion = parentComponentTracer.getComponentSpanAssertion();
  }

  @Override
  public Optional<Span> startSpan(CoreEvent coreEvent) {
    return coreEventTracer.startSpan(coreEvent, initialSpanInfo, parentComponentSpanAssertion);
  }

  @Override
  public void endCurrentSpan(CoreEvent event) {
    coreEventTracer.endCurrentSpan(event, componentSpanAssertion);
  }

  @Override
  public void addCurrentSpanAttribute(CoreEvent event, String key, String value) {
    coreEventTracer.addCurrentSpanAttribute(event, key, value);
  }

  @Override
  public Assertion getComponentSpanAssertion() {
    return componentSpanAssertion;
  }

}
