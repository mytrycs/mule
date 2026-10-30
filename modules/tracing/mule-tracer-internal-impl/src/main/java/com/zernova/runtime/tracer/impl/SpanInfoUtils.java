/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.impl;

import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.tracer.api.span.info.EnrichedInitialSpanInfo;
import com.zernova.runtime.tracer.api.span.info.InitialSpanInfo;

public class SpanInfoUtils {

  public static EnrichedInitialSpanInfo enrichInitialSpanInfo(InitialSpanInfo spanCustomizationInfo, CoreEvent coreEvent) {
    return new CoreEventEnrichedInitialSpanInfo(spanCustomizationInfo, coreEvent);
  }

}
