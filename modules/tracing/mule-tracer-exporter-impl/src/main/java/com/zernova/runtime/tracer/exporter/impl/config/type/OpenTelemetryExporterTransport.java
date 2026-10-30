/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.exporter.impl.config.type;

import com.zernova.runtime.tracer.exporter.impl.optel.resources.SpanExporterConfigurator;
import com.zernova.runtime.tracer.exporter.impl.optel.resources.grpc.GrpcSpanExporterConfigurator;
import com.zernova.runtime.tracer.exporter.impl.optel.resources.http.HttpSpanExporterConfigurator;

/**
 * The transport of export of the spans.
 *
 * @since 4.5.0
 */
public enum OpenTelemetryExporterTransport {

  GRPC(new GrpcSpanExporterConfigurator()),

  HTTP(new HttpSpanExporterConfigurator());

  private final SpanExporterConfigurator spanExporterConfigurator;

  OpenTelemetryExporterTransport(SpanExporterConfigurator spanExporterConfigurator) {
    this.spanExporterConfigurator = spanExporterConfigurator;
  }

  public SpanExporterConfigurator getSpanExporterConfigurator() {
    return spanExporterConfigurator;
  }
}
