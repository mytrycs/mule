/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metrics.exporter.impl.config;

import com.zernova.runtime.metrics.exporter.impl.optel.resources.MeterExporterConfigurator;
import com.zernova.runtime.metrics.exporter.impl.optel.resources.grpc.GrpcMeterExporterConfigurator;
import com.zernova.runtime.metrics.exporter.impl.optel.resources.http.HttpMeterExporterConfigurator;
import com.zernova.runtime.metrics.exporter.impl.optel.resources.inmemory.InMemoryMeterExporterConfigurator;

/**
 * Specifies different ways in which metrics can be exported.
 *
 * @since 4.5.0
 */
public enum OpenTelemetryMeterExporterTransport {

  GRPC(new GrpcMeterExporterConfigurator()),

  HTTP(new HttpMeterExporterConfigurator()),

  IN_MEMORY(new InMemoryMeterExporterConfigurator());

  private final MeterExporterConfigurator meterExporterConfigurator;

  OpenTelemetryMeterExporterTransport(MeterExporterConfigurator meterExporterConfigurator) {
    this.meterExporterConfigurator = meterExporterConfigurator;
  }

  public MeterExporterConfigurator getMeterExporterConfigurator() {
    return meterExporterConfigurator;
  }
}
