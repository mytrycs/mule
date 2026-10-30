/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.api.span.validation;

import com.zernova.runtime.api.profiling.tracing.Span;
import com.zernova.runtime.tracer.api.EventTracer;

/**
 * An assertion to make on a {@link Span}.
 *
 * @since 4.5.0
 */
public interface Assertion {

  Assertion SUCCESSFUL_ASSERTION = span -> {
  };

  /**
   * Assertion to perform on the span. If the span does not meet certain conditions an exception must be raised. This will be
   * invoked internally by the {@link EventTracer}.
   *
   * @param span the span to perform the assertion on.
   * @throws AssertionFailedException thrown if the assertion fails.
   */
  void assertOnSpan(Span span) throws AssertionFailedException;
}
