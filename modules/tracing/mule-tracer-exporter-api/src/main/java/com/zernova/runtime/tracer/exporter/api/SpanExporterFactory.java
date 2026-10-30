/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.exporter.api;

import com.zernova.runtime.api.profiling.tracing.Span;
import com.zernova.runtime.tracer.api.sniffer.SpanSnifferManager;
import com.zernova.runtime.tracer.api.span.exporter.SpanExporter;
import com.zernova.runtime.tracer.api.span.info.InitialSpanInfo;

/**
 * A factory for {@link SpanExporter}
 *
 * @since 4.5.0
 */
public interface SpanExporterFactory {

  /**
   * @param span            the {@link Span} to get the exporter for.
   * @param initialSpanInfo the {@link InitialSpanInfo}.
   *
   * @return a {@link SpanExporter} for the {@link Span}.
   */
  SpanExporter getSpanExporter(Span span, InitialSpanInfo initialSpanInfo);

  /**
   * @return a {@link SpanSnifferManager}.
   */
  SpanSnifferManager getSpanSnifferManager();
}
