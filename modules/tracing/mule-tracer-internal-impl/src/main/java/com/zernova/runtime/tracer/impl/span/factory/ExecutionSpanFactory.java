/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.impl.span.factory;

import static com.zernova.runtime.tracer.impl.span.InternalSpan.getAsInternalSpan;

import com.zernova.runtime.tracer.api.sniffer.SpanSnifferManager;
import com.zernova.runtime.tracer.api.context.SpanContext;
import com.zernova.runtime.tracer.impl.span.InternalSpan;
import com.zernova.runtime.tracer.api.span.info.InitialSpanInfo;
import com.zernova.runtime.tracer.exporter.api.SpanExporterFactory;
import com.zernova.runtime.tracer.impl.span.ExportOnEndExecutionSpan;

import jakarta.inject.Inject;

public class ExecutionSpanFactory implements EventSpanFactory {

  @Inject
  private SpanExporterFactory spanExporterFactory;

  @Override
  public InternalSpan getSpan(SpanContext spanContext,
                              InitialSpanInfo initialSpanInfo) {
    return ExportOnEndExecutionSpan.createExportOnEndExecutionSpan(spanExporterFactory,
                                                                   getAsInternalSpan(spanContext.getSpan().orElse(null)),
                                                                   initialSpanInfo);
  }

  @Override
  public SpanSnifferManager getSpanSnifferManager() {
    return spanExporterFactory.getSpanSnifferManager();
  }
}
