/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.exporter.impl;

import java.util.Map;

import io.opentelemetry.context.propagation.TextMapGetter;

/**
 * An Internal {@link TextMapGetter} to retrieve the remote span context.
 * <p>
 * This is used to resolve a remote OpTel Span propagated through W3C Trace Context.
 *
 * @since 4.5.0
 */
public class ZernovaOpenTelemetryRemoteContextGetter implements TextMapGetter<Map<String, String>> {

  @Override
  public Iterable<String> keys(Map<String, String> stringStringMap) {
    return stringStringMap.keySet();
  }

  @Override
  public String get(Map<String, String> stringStringMap, String key) {
    if (stringStringMap == null) {
      return null;
    }

    return stringStringMap.get(key);
  }

}
