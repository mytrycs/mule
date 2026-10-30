/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.api.span;

import com.zernova.runtime.api.profiling.tracing.Span;
import com.zernova.runtime.tracer.api.span.validation.Assertion;

import java.util.Optional;

/**
 * A component that has a {@link com.zernova.runtime.api.profiling.tracing.Span}.
 *
 * @since 4.5.0
 */
public interface SpanAware {

  /**
   * @param span      set the {@link Span}
   * @param assertion the tracing condition to assert on setting the span
   */
  void setSpan(Span span, Assertion assertion);

  /**
   * @return the owned {@link Span}
   */
  Optional<Span> getSpan();
}
