/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metrics.exporter.impl.optel.config;

import static com.zernova.runtime.metrics.exporter.config.api.OpenTelemetryMeterExporterConfigurationProperties.MULE_OPEN_TELEMETRY_METER_EXPORTER_ENABLED;
import static com.zernova.runtime.metrics.exporter.config.api.OpenTelemetryMeterExporterConfigurationProperties.MULE_OPEN_TELEMETRY_METER_EXPORTER_ENDPOINT;
import static com.zernova.runtime.metrics.exporter.config.api.OpenTelemetryMeterExporterConfigurationProperties.MULE_OPEN_TELEMETRY_METER_EXPORTER_INTERVAL;
import static com.zernova.runtime.metrics.exporter.config.api.OpenTelemetryMeterExporterConfigurationProperties.MULE_OPEN_TELEMETRY_METER_EXPORTER_TIMEOUT;
import static com.zernova.runtime.metrics.exporter.config.api.OpenTelemetryMeterExporterConfigurationProperties.MULE_OPEN_TELEMETRY_METER_EXPORTER_TYPE;

import com.zernova.runtime.ast.api.exception.PropertyNotFoundException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.metrics.exporter.config.api.MeterExporterConfiguration;
import com.zernova.runtime.metrics.exporter.config.impl.FileMeterExporterConfiguration;

import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;

public class OpenTelemetryAutoConfigurableMeterExporterConfiguration implements MeterExporterConfiguration {

  @Inject
  private ZernovaContext muleContext;

  private static final String GRPC_EXPORTER_TYPE = "GRPC";
  private static final String DEFAULT_EXPORTER_TYPE = GRPC_EXPORTER_TYPE;
  private static final String DEFAULT_GRPC_EXPORTER_ENDPOINT = "http://localhost:4317";
  private static final String DEFAULT_HTTP_EXPORTER_ENDPOINT = "http://localhost:4318/v1/metrics";
  private static final String DEFAULT_EXPORTER_TIMEOUT = "10000";
  private static final String DEFAULT_EXPORTER_INTERVAL = "60";
  private static final String DEFAULT_ENABLED_VALUE = "false";

  private MeterExporterConfiguration delegate;
  private final Map<String, String> defaultConfigurationValues = new HashMap<>();

  /**
   * This constructor is needed for injection in the registry.
   */
  public OpenTelemetryAutoConfigurableMeterExporterConfiguration() {}

  public OpenTelemetryAutoConfigurableMeterExporterConfiguration(MeterExporterConfiguration delegate) {
    this.delegate = delegate;
    initialiseDefaultConfigurationValues();
  }

  @Override
  public String getStringValue(String key) {
    try {
      if (delegate == null) {
        this.delegate = new FileMeterExporterConfiguration(muleContext);
        initialiseDefaultConfigurationValues();
      }
      return delegate.getStringValue(key, defaultConfigurationValues.get(key));
    } catch (PropertyNotFoundException e) {
      return defaultConfigurationValues.get(key);
    }
  }

  private void initialiseDefaultConfigurationValues() {
    defaultConfigurationValues.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_ENABLED, DEFAULT_ENABLED_VALUE);
    defaultConfigurationValues.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_TYPE, DEFAULT_EXPORTER_TYPE);
    if (getStringValue(MULE_OPEN_TELEMETRY_METER_EXPORTER_TYPE).equals(GRPC_EXPORTER_TYPE)) {
      defaultConfigurationValues.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_ENDPOINT, DEFAULT_GRPC_EXPORTER_ENDPOINT);
    } else {
      defaultConfigurationValues.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_ENDPOINT, DEFAULT_HTTP_EXPORTER_ENDPOINT);
    }
    defaultConfigurationValues.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_TIMEOUT, DEFAULT_EXPORTER_TIMEOUT);
    defaultConfigurationValues.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_INTERVAL, DEFAULT_EXPORTER_INTERVAL);
  }
}
