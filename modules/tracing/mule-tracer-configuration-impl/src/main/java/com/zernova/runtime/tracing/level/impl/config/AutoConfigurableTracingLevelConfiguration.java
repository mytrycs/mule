/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracing.level.impl.config;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.tracer.exporter.config.api.OpenTelemetrySpanExporterConfigurationProperties.MULE_OPEN_TELEMETRY_EXPORTER_DEFAULT_TRACING_LEVEL;
import static com.zernova.runtime.tracer.exporter.config.api.OpenTelemetrySpanExporterConfigurationProperties.MULE_OPEN_TELEMETRY_EXPORTER_ENABLED;
import static com.zernova.runtime.tracing.level.api.config.TracingLevel.MONITORING;
import static com.zernova.runtime.tracing.level.api.config.TracingLevel.valueOf;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.util.Objects.requireNonNull;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.tracer.exporter.config.api.OpenTelemetrySpanExporterConfigurationProperties;
import com.zernova.runtime.tracer.exporter.config.api.SpanExporterConfiguration;
import com.zernova.runtime.tracing.level.api.config.TracingLevel;
import com.zernova.runtime.tracing.level.api.config.TracingLevelConfiguration;

import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.inject.Inject;

import org.slf4j.Logger;

/**
 * Autoconfiguration of tracing level. Will use the default level.
 *
 * @see OpenTelemetrySpanExporterConfigurationProperties#MULE_OPEN_TELEMETRY_EXPORTER_DEFAULT_TRACING_LEVEL
 */
public class AutoConfigurableTracingLevelConfiguration implements TracingLevelConfiguration, Disposable {

  private static final Logger LOGGER = getLogger(AutoConfigurableTracingLevelConfiguration.class);

  private ZernovaContext muleContext;
  private SpanExporterConfiguration spanExporterConfiguration;
  private TracingLevelConfiguration delegate;
  private TracingLevel defaultLevel =
      valueOf(getProperty(MULE_OPEN_TELEMETRY_EXPORTER_DEFAULT_TRACING_LEVEL, MONITORING.toString()).toUpperCase());

  @Inject
  public AutoConfigurableTracingLevelConfiguration(ZernovaContext muleContext) {
    this(muleContext, new FileTracingLevelConfiguration(muleContext));
  }

  protected AutoConfigurableTracingLevelConfiguration(ZernovaContext muleContext, TracingLevelConfiguration delegate) {
    requireNonNull(delegate, "delegate cannot be null");

    this.muleContext = muleContext;
    this.delegate = delegate;
  }

  @Inject
  public void setSpanExporterConfiguration(SpanExporterConfiguration spanExporterConfiguration) {
    this.spanExporterConfiguration = spanExporterConfiguration;
  }

  @Override
  public TracingLevel getTracingLevel() {
    return getTracingLevelFromDelegate(() -> delegate.getTracingLevel());
  }

  @Override
  public TracingLevel getTracingLevelOverride(String location) {
    return getTracingLevelFromDelegate(() -> delegate.getTracingLevelOverride(location));
  }

  private TracingLevel getTracingLevelFromDelegate(Supplier<TracingLevel> tracingLevelSupplier) {
    try {
      TracingLevel level = tracingLevelSupplier.get();
      return level != null ? level : defaultLevel;
    } catch (ZernovaRuntimeException e) {
      if (parseBoolean(spanExporterConfiguration.getStringValue(MULE_OPEN_TELEMETRY_EXPORTER_ENABLED, "false"))) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER
              .debug(format("Non existent or non parseable tracing level config file found. The tracing level will be set to the default: %s",
                            defaultLevel),
                     e);
        } else {
          LOGGER
              .info(format("Non existent or non parseable tracing level config file found. The tracing level will be set to the default: %s. Enable DEBUG log level to see the exception",
                           defaultLevel));
        }
      }
    }
    return defaultLevel;
  }

  @Override
  public void onConfigurationChange(Consumer<TracingLevelConfiguration> onConfigurationChangeConsumer) {
    delegate.onConfigurationChange(onConfigurationChangeConsumer);
  }

  @Override
  public void dispose() {
    disposeIfNeeded(delegate, LOGGER);
  }
}
