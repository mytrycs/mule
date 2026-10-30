/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling;

import com.zernova.runtime.tracer.exporter.config.api.SpanExporterConfiguration;

/**
 * A {@link SpanExporterConfiguration} that is empty (no properties).
 *
 * @since 4.5.0
 */
public class EmptySpanExporterConfiguration implements SpanExporterConfiguration {

  @Override
  public String getStringValue(String key) {
    return null;
  }
}
