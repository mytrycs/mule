/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling;

import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.api.profiling.tracing.Span;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.tracer.api.EventTracer;
import com.zernova.runtime.tracer.api.context.getter.DistributedTraceContextGetter;
import com.zernova.runtime.tracer.api.sniffer.SpanSnifferManager;
import com.zernova.runtime.tracer.api.span.info.InitialSpanInfo;
import com.zernova.runtime.tracer.api.span.validation.Assertion;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.empty;

/**
 * A noop {@link NoopCoreEventTracer}
 *
 * @since 4.5.0
 */
public class NoopCoreEventTracer implements EventTracer<CoreEvent> {

  public static EventTracer<CoreEvent> getNoopCoreEventTracer() {
    return new NoopCoreEventTracer();
  }

  private NoopCoreEventTracer() {}

  @Override
  public Optional<Span> startSpan(CoreEvent event, InitialSpanInfo initialSpanInfo) {
    return empty();
  }

  @Override
  public Optional<Span> startSpan(CoreEvent event, InitialSpanInfo initialSpanInfo, Assertion assertion) {
    return empty();
  }

  @Override
  public void endCurrentSpan(CoreEvent event) {
    // Noop
  }

  @Override
  public void endCurrentSpan(CoreEvent event, Assertion condition) {
    // Noop
  }

  @Override
  public void injectDistributedTraceContext(EventContext eventContext,
                                            DistributedTraceContextGetter distributedTraceContextGetter) {
    // Noop
  }

  @Override
  public void recordErrorAtCurrentSpan(CoreEvent event, Supplier<Error> errorSupplier, boolean isErrorEscapingCurrentSpan) {
    // Noop
  }

  @Override
  public void setCurrentSpanName(CoreEvent event, String name) {
    // Noop
  }

  @Override
  public void addCurrentSpanAttribute(CoreEvent event, String key, String value) {
    // Noop
  }

  @Override
  public void addCurrentSpanAttributes(CoreEvent event, Map<String, String> attributes) {
    // Noop
  }

  @Override
  public SpanSnifferManager getSpanSnifferManager() {
    return new NoopSpanSnifferManager();
  }

  private static class NoopSpanSnifferManager implements SpanSnifferManager {

  }
}
