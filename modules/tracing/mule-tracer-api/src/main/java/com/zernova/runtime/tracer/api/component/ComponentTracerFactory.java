/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.api.component;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.api.profiling.tracing.Span;

/**
 * Component Tracer Factory for creating {@link ComponentTracer}s.
 *
 * @since 4.5.0
 */
public interface ComponentTracerFactory<T extends Event> {

  /**
   * Creates the {@link ComponentTracer} from the {@link Component} processor that will start a {@link Span}.
   *
   * @param component the {@link Component} processor.
   * @return the {@link ComponentTracer} generated from the {@link Component}.
   */
  ComponentTracer<T> fromComponent(Component component);

  /**
   * Creates the {@link ComponentTracer} from the {@link Component} processor that will start a {@link Span}.
   *
   * @param component             the {@link Component} processor.
   * @param parentComponentTracer the {@link ComponentTracer} of the parent {@link Component} processor.
   * @return the {@link ComponentTracer} generated from the {@link Component}.
   */
  ComponentTracer<T> fromComponent(Component component, ComponentTracer<?> parentComponentTracer);

  /**
   * Creates the {@link ComponentTracer} from the {@link Component} processor that will start a {@link Span}.
   *
   * @param component the {@link Component} processor.
   * @param suffix    the suffix of the {@link Component} name.
   * @return the {@link ComponentTracer} generated from the {@link Component}.
   */
  ComponentTracer<T> fromComponent(Component component, String suffix);

  /**
   * Creates the {@link ComponentTracer} from the {@link Component} processor that will start a {@link Span}.
   *
   * @param component      the {@link Component} processor.
   * @param overriddenName the overridden name of the {@link Component} name.
   * @param suffix         the suffix of the {@link Component} name.
   * @return the {@link ComponentTracer} generated from the {@link Component}.
   */
  ComponentTracer<T> fromComponent(Component component, String overriddenName, String suffix);

}
