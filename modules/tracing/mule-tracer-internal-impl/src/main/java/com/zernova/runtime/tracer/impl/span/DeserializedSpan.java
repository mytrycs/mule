/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.impl.span;


import com.zernova.runtime.tracer.api.context.getter.DistributedTraceContextGetter;
import com.zernova.runtime.tracer.impl.context.extractor.RuntimeEventTraceExtractors;
import com.zernova.runtime.tracer.impl.context.extractor.TraceContextFieldExtractor;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.copyOf;

public class DeserializedSpan extends RootInternalSpan {

  private static final TraceContextFieldExtractor TRACING_FIELD_EXTRACTOR =
      RuntimeEventTraceExtractors.getDefaultTraceContextFieldsExtractor();
  private static final TraceContextFieldExtractor BAGGAGE_ITEMS_EXTRACTOR =
      RuntimeEventTraceExtractors.getDefaultBaggageExtractor();

  public static InternalSpan getDeserializedRootSpan(DistributedTraceContextGetter distributedTraceContextGetter,
                                                     boolean managedChildSpan) {
    Map<String, String> mapSerialization = new HashMap<>();
    mapSerialization.putAll(TRACING_FIELD_EXTRACTOR.extract(distributedTraceContextGetter));
    mapSerialization.putAll(BAGGAGE_ITEMS_EXTRACTOR.extract(distributedTraceContextGetter));
    return new DeserializedSpan(copyOf(mapSerialization), managedChildSpan);
  }

  private final Map<String, String> mapSerialization;


  private DeserializedSpan(Map<String, String> mapSerialization, boolean managedChildSpan) {
    this.mapSerialization = mapSerialization;
    this.managedChildSpan = managedChildSpan;
  }

  @Override
  public Map<String, String> serializeAsMap() {
    return mapSerialization;
  }
}
