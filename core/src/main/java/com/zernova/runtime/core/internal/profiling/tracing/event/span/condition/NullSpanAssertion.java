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
 * A {@link Assertion} that verifies that there is no span set.
 *
 * @since 4.5.0
 */
public class NullSpanAssertion implements Assertion {

  private static final Assertion INSTANCE = new NullSpanAssertion();

  public static Assertion getNullSpanTracingCondition() {
    return INSTANCE;
  }

  private NullSpanAssertion() {}

  @Override
  public void assertOnSpan(Span span) throws AssertionFailedException {
    if (span != null) {
      throw new AssertionFailedException("Span with name: " + span.getName()
          + " was found while no span was expected.");
    }
  }
}
