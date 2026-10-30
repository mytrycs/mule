/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.api.context;

import static java.util.Optional.empty;

import com.zernova.runtime.api.profiling.tracing.Span;
import com.zernova.runtime.tracer.api.span.SpanAware;
import com.zernova.runtime.tracer.api.span.error.InternalSpanError;
import com.zernova.runtime.tracer.api.span.validation.Assertion;

import java.util.Optional;

/**
 * Represents a context defined by a {@link Span}.
 *
 * @since 1.5.0
 */
public interface SpanContext extends SpanAware {

  static SpanContext emptySpanContext() {
    return new SpanContext() {

      @Override
      public void setSpan(Span span, Assertion assertion) {
        // Nothing to do.
      }

      @Override
      public Optional<Span> getSpan() {
        return empty();
      }

      @Override
      public SpanContext copy() {
        return this;
      }

      @Override
      public void endSpan(Assertion assertion) {
        // Nothing to do.
      }

      @Override
      public void recordErrorAtSpan(InternalSpanError error) {
        // Nothing to do.
      }
    };
  }

  /**
   * @return a copy of the {@link SpanContext}
   */
  SpanContext copy();

  /**
   * @param assertion the {@link Assertion} to verify on end.
   */
  void endSpan(Assertion assertion);

  /**
   * @param error the error to record.
   */
  void recordErrorAtSpan(InternalSpanError error);

}
