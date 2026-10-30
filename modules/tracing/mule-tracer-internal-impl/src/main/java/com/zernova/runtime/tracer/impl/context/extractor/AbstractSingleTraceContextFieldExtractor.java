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
 * A {@link TraceContextFieldExtractor} for a single field.
 *
 * @since 4.5.0
 */
public abstract class AbstractSingleTraceContextFieldExtractor implements TraceContextFieldExtractor {

  @Override
  public Map<String, String> extract(DistributedTraceContextGetter distributedTraceContextMapGetter) {
    Map<String, String> resultContext = new HashMap<>();
    distributedTraceContextMapGetter.get(getFieldKey()).ifPresent(value -> resultContext.put(getFieldKey(), value));

    return resultContext;
  }

  /**
   * @return the key for the field to extract.
   */
  public abstract String getFieldKey();
}
