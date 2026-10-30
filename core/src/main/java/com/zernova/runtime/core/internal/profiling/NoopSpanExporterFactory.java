/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling;

import static com.zernova.runtime.tracer.api.span.exporter.SpanExporter.NOOP_EXPORTER;

import com.zernova.runtime.api.profiling.tracing.Span;
import com.zernova.runtime.tracer.api.sniffer.SpanSnifferManager;
import com.zernova.runtime.tracer.api.span.exporter.SpanExporter;
import com.zernova.runtime.tracer.api.span.info.InitialSpanInfo;
import com.zernova.runtime.tracer.exporter.api.SpanExporterFactory;

/**
 * A noop {@link SpanExporterFactory}.
 *
 * @since 4.5.0
 */
public class NoopSpanExporterFactory implements SpanExporterFactory {

  @Override
  public SpanExporter getSpanExporter(Span span, InitialSpanInfo initialSpanInfo) {
    return NOOP_EXPORTER;
  }

  @Override
  public SpanSnifferManager getSpanSnifferManager() {
    return new NoOpSpanSnifferManager();
  }

  private static class NoOpSpanSnifferManager implements SpanSnifferManager {

  }
}
