/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling.tracing.event.span.condition;

import com.zernova.runtime.api.profiling.tracing.Span;
import com.zernova.runtime.tracer.api.span.validation.Assertion;
import com.zernova.runtime.tracer.api.span.validation.AssertionFailedException;

/**
 * A {@link Assertion} that indicates that the current span name must be the one indicated. If it is not, a
 * {@link AssertionFailedException} will be raised.
 *
 * @since 4.5.0
 */
public class ComponentSpanNameAssertion implements Assertion {

  private final Assertion delegate;

  /**
   * @param expectedSpanName the expected span name.
   */
  public ComponentSpanNameAssertion(String expectedSpanName) {
    if (expectedSpanName != null) {
      this.delegate = new SpanNameAssertion(expectedSpanName);
    } else {
      this.delegate = SUCCESSFUL_ASSERTION;
    }

  }

  @Override
  public void assertOnSpan(Span span) throws AssertionFailedException {
    delegate.assertOnSpan(span);
  }
}
