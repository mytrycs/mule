/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.exporter.impl;

import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static com.zernova.runtime.tracer.exporter.config.api.OpenTelemetrySpanExporterConfigurationProperties.ALWAYS_ON_SAMPLER;
import static com.zernova.runtime.tracer.exporter.config.api.OpenTelemetrySpanExporterConfigurationProperties.MULE_OPEN_TELEMETRY_OTEL_TRACES_SAMPLER;
import static com.zernova.test.allure.AllureConstants.Profiling.PROFILING;
import static com.zernova.test.allure.AllureConstants.Profiling.ProfilingServiceStory.OPEN_TELEMETRY_EXPORTER;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.ZernovaContext;

import io.opentelemetry.sdk.trace.SpanProcessor;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Test;
import com.zernova.runtime.tracer.exporter.config.api.OpenTelemetrySpanExporterConfigurationProperties;
import com.zernova.runtime.tracer.exporter.config.api.SpanExporterConfiguration;

@Feature(PROFILING)
@Story(OPEN_TELEMETRY_EXPORTER)
public class OpenTelemetrySpanExporterFactoryTestCase {

  @Test
  public void disposeMustShutdownSpanProcessor() throws Exception {
    ZernovaContext muleContext = mock(ZernovaContext.class);
    ZernovaConfiguration muleConfiguration = mock(ZernovaConfiguration.class);
    FeatureFlaggingService featureFlaggingService = mock(FeatureFlaggingService.class);
    SpanProcessor mockedSpanProcessor = mock(SpanProcessor.class);
    SpanExporterConfiguration spanExporterConfiguration = mock(SpanExporterConfiguration.class);

    when(spanExporterConfiguration.getStringValue(MULE_OPEN_TELEMETRY_OTEL_TRACES_SAMPLER)).thenReturn(ALWAYS_ON_SAMPLER);
    when(muleConfiguration.getId()).thenReturn(randomNumeric(3));
    when(muleContext.getConfiguration()).thenReturn(muleConfiguration);
    when(muleContext.getArtifactType()).thenReturn(APP);

    OpenTelemetrySpanExporterFactory spanExporterFactory = new OpenTelemetrySpanExporterFactory() {

      @Override
      protected SpanProcessor resolveOpenTelemetrySpanProcessor() {
        return mockedSpanProcessor;
      }
    };

    spanExporterFactory.setMuleContext(muleContext);
    spanExporterFactory.setConfiguration(spanExporterConfiguration);
    spanExporterFactory.setFeatureFlaggingService(featureFlaggingService);
    spanExporterFactory.initialise();
    spanExporterFactory.dispose();
    verify(mockedSpanProcessor, times(1)).shutdown();
  }

}
