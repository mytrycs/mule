/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.impl.context.extractor;

import com.zernova.runtime.tracer.api.context.getter.DistributedTraceContextGetter;

import java.util.Map;

/**
 * An extractor which returns a map containing keys/values corresponding related to a trace context.
 *
 * @since 4.5.0
 */
public interface TraceContextFieldExtractor {

  /**
   * @param distributedTraceContextMapGetter a {@link DistributedTraceContextGetter} through which the context is propagated.
   *
   * @return the map containing the specific key/values.
   */
  Map<String, String> extract(DistributedTraceContextGetter distributedTraceContextMapGetter);
}
