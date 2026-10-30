/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.impl.context.extractor;

import com.zernova.runtime.tracer.api.context.getter.DistributedTraceContextGetter;

import java.util.HashMap;
import java.util.Map;

/**
 * A composed {@link TraceContextFieldExtractor}. It extracts all the fields extracted by the extractors passed as parameters.
 *
 * @since 4.5.0
 */
public class ComposedTraceContextFieldExtractor implements TraceContextFieldExtractor {

  private final TraceContextFieldExtractor[] traceContextFieldExtractors;

  public ComposedTraceContextFieldExtractor(TraceContextFieldExtractor... traceContextFieldExtractors) {
    this.traceContextFieldExtractors = traceContextFieldExtractors;
  }

  @Override
  public Map<String, String> extract(DistributedTraceContextGetter distributedTraceContextMapGetter) {
    Map<String, String> result = new HashMap<>();

    for (TraceContextFieldExtractor traceContextFieldExtractor : traceContextFieldExtractors) {
      result.putAll(traceContextFieldExtractor.extract(distributedTraceContextMapGetter));
    }

    return result;
  }
}
