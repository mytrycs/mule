/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.profiling.tracing.Span;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.tracer.api.component.ComponentTracer;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;
import com.zernova.runtime.tracer.api.span.validation.Assertion;

import java.util.Optional;

/**
 * A dummy implementation of {@link ComponentTracerFactory}.
 *
 * @since 4.5.0
 */
public class DummyComponentTracerFactory implements ComponentTracerFactory<CoreEvent> {

  private static final DummyComponentTracerFactory INSTANCE = new DummyComponentTracerFactory();

  public static final ComponentTracer<CoreEvent> DUMMY_COMPONENT_TRACER_INSTANCE = new DummyComponentTracer();

  public static DummyComponentTracerFactory getDummyComponentTracerFactory() {
    return INSTANCE;
  }

  @Override
  public ComponentTracer<CoreEvent> fromComponent(Component component) {
    return DUMMY_COMPONENT_TRACER_INSTANCE;
  }

  @Override
  public ComponentTracer<CoreEvent> fromComponent(Component component, ComponentTracer<?> parentComponentTracer) {
    return DUMMY_COMPONENT_TRACER_INSTANCE;
  }

  @Override
  public ComponentTracer<CoreEvent> fromComponent(Component component, String suffix) {
    return DUMMY_COMPONENT_TRACER_INSTANCE;
  }

  @Override
  public ComponentTracer<CoreEvent> fromComponent(Component component, String overriddenName, String suffix) {
    return DUMMY_COMPONENT_TRACER_INSTANCE;
  }

  private static class DummyComponentTracer implements ComponentTracer<CoreEvent> {

    @Override
    public Optional<Span> startSpan(CoreEvent event) {
      return Optional.empty();
    }

    @Override
    public void endCurrentSpan(CoreEvent event) {
      // Nothing to do.
    }

    @Override
    public void addCurrentSpanAttribute(CoreEvent event, String key, String value) {
      // Nothing to do.
    }

    @Override
    public Assertion getComponentSpanAssertion() {
      return null;
    }
  }
}
