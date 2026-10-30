/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.impl.context.extractor.w3c;

import static com.zernova.runtime.tracer.impl.context.extractor.w3c.TraceParentContextFieldExtractor.TRACEPARENT;
import static com.zernova.test.allure.AllureConstants.EventContextFeature.EVENT_CONTEXT;
import static com.zernova.test.allure.AllureConstants.EventContextFeature.EventContextStory.DISTRIBUTED_TRACE_CONTEXT;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import com.zernova.runtime.tracer.impl.context.extractor.TraceContextFieldExtractor;

@Feature(EVENT_CONTEXT)
@Story(DISTRIBUTED_TRACE_CONTEXT)
public class TraceParentContextFieldExtractorTestCase extends AbstractW3CTraceContextExtractorTestCase {

  @Override
  public String getTraceField() {
    return TRACEPARENT;
  }

  @Override
  public TraceContextFieldExtractor getTraceContextFieldExtractor() {
    return new TraceParentContextFieldExtractor();
  }
}
