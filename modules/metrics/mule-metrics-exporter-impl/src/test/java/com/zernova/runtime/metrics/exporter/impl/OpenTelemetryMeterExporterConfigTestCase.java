/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metrics.exporter.impl;

import static com.zernova.runtime.metrics.exporter.config.api.OpenTelemetryMeterExporterConfigurationProperties.MULE_OPEN_TELEMETRY_METER_EXPORTER_CA_FILE_LOCATION;
import static com.zernova.runtime.metrics.exporter.config.api.OpenTelemetryMeterExporterConfigurationProperties.MULE_OPEN_TELEMETRY_METER_EXPORTER_CERT_FILE_LOCATION;
import static com.zernova.runtime.metrics.exporter.config.api.OpenTelemetryMeterExporterConfigurationProperties.MULE_OPEN_TELEMETRY_METER_EXPORTER_COMPRESSION_TYPE;
import static com.zernova.runtime.metrics.exporter.config.api.OpenTelemetryMeterExporterConfigurationProperties.MULE_OPEN_TELEMETRY_METER_EXPORTER_ENABLED;
import static com.zernova.runtime.metrics.exporter.config.api.OpenTelemetryMeterExporterConfigurationProperties.MULE_OPEN_TELEMETRY_METER_EXPORTER_ENDPOINT;
import static com.zernova.runtime.metrics.exporter.config.api.OpenTelemetryMeterExporterConfigurationProperties.MULE_OPEN_TELEMETRY_METER_EXPORTER_HEADERS;
import static com.zernova.runtime.metrics.exporter.config.api.OpenTelemetryMeterExporterConfigurationProperties.MULE_OPEN_TELEMETRY_METER_EXPORTER_INTERVAL;
import static com.zernova.runtime.metrics.exporter.config.api.OpenTelemetryMeterExporterConfigurationProperties.MULE_OPEN_TELEMETRY_METER_EXPORTER_KEY_FILE_LOCATION;
import static com.zernova.runtime.metrics.exporter.config.api.OpenTelemetryMeterExporterConfigurationProperties.MULE_OPEN_TELEMETRY_METER_EXPORTER_TLS_ENABLED;
import static com.zernova.runtime.metrics.exporter.config.api.OpenTelemetryMeterExporterConfigurationProperties.MULE_OPEN_TELEMETRY_METER_EXPORTER_TYPE;
import static com.zernova.runtime.metrics.exporter.impl.config.OpenTelemetryMeterExporterTransport.GRPC;
import static com.zernova.runtime.metrics.exporter.impl.config.OpenTelemetryMeterExporterTransport.HTTP;

import static java.lang.Boolean.TRUE;

import static org.mockito.Mockito.mock;
import static com.zernova.runtime.metrics.exporter.impl.utils.TestServerRule.GRPC_ENDPOINT_PATH;
import static com.zernova.runtime.metrics.exporter.impl.utils.TestServerRule.HTTP_ENDPOINT_PATH;
import static com.zernova.runtime.metrics.exporter.impl.utils.TestServerRule.HTTP_GZIP_ENDPOINT_PATH;

import com.zernova.runtime.metrics.api.instrument.LongCounter;
import com.zernova.runtime.metrics.api.meter.Meter;
import com.zernova.runtime.metrics.exporter.api.MeterExporter;
import com.zernova.runtime.metrics.exporter.config.api.MeterExporterConfiguration;
import com.zernova.runtime.metrics.exporter.impl.optel.config.OpenTelemetryAutoConfigurableMeterExporterConfiguration;
import com.zernova.runtime.metrics.exporter.impl.utils.TestExportedMeter;
import com.zernova.runtime.metrics.exporter.impl.utils.TestMeterExporterConfiguration;
import com.zernova.runtime.metrics.exporter.impl.utils.TestOpenTelemetryMeterExporterFactory;
import com.zernova.runtime.metrics.exporter.impl.utils.TestServerRule;
import com.zernova.runtime.metrics.impl.meter.DefaultMeter;
import com.zernova.tck.probe.JUnitProbe;
import com.zernova.tck.probe.PollingProber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.linecorp.armeria.testing.junit4.server.SelfSignedCertificateRule;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class OpenTelemetryMeterExporterConfigTestCase {

  private static final int TIMEOUT_MILLIS = 30000;
  private static final int POLL_DELAY_MILLIS = 100;
  private static final String METER_EXPORTER_INTERVAL = "1";
  private static final String METER_NAME = "testMetricName";
  private static final String LONG_COUNTER_NAME = "long-counter-test";
  private static final String LONG_COUNTER_DESCRIPTION = "Long Counter test";
  private static final String UNIT_NAME = "test-unit";

  @Rule
  public final TestServerRule server = new TestServerRule();

  private MeterExporter openTelemetryMeterExporter;
  private Meter meter;
  private LongCounter longCounter;

  @ClassRule
  public static SelfSignedCertificateRule serverTls = new SelfSignedCertificateRule();

  @ClassRule
  public static SelfSignedCertificateRule clientTls = new SelfSignedCertificateRule();

  @Before
  public void setUp() {
    MeterExporter meterExporter = mock(MeterExporter.class);
    meter = DefaultMeter.builder(METER_NAME)
        .withMeterExporter(meterExporter)
        .build();

    longCounter = meter.counterBuilder(LONG_COUNTER_NAME)
        .withDescription(LONG_COUNTER_DESCRIPTION)
        .withUnit(UNIT_NAME)
        .build();
  }

  @After
  public void after() {
    server.reset();
    openTelemetryMeterExporter.dispose();
  }

  @Test
  public void defaultGrpcExporterShouldExportLongCounterMetricSuccessfully() {
    String meterExporterEndpoint = "http://localhost:" + server.httpPort();
    Map<String, String> properties = new HashMap<>();
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_ENABLED, TRUE.toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_ENDPOINT, meterExporterEndpoint);
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_INTERVAL, METER_EXPORTER_INTERVAL);

    MeterExporterConfiguration configuration = getMeterExporterConfiguration(properties);
    OpenTelemetryMeterExporterFactory openTelemetryMeterExporterFactory = new TestOpenTelemetryMeterExporterFactory();
    openTelemetryMeterExporter = openTelemetryMeterExporterFactory.getMeterExporter(configuration);

    openTelemetryMeterExporter.registerMeterToExport(meter);
    openTelemetryMeterExporter.enableExport(longCounter);
    longCounter.add(4);

    PollingProber prober = new PollingProber(TIMEOUT_MILLIS, POLL_DELAY_MILLIS);
    prober.check(new JUnitProbe() {

      @Override
      protected boolean test() {
        return getMetricsByCounterName(server.getMetrics(), longCounter.getName()).size() >= 1;
      }

      @Override
      public String describeFailure() {
        return "The expected amount of metrics was not captured.";
      }
    });
  }

  @Test
  public void defaultHttpExporterShouldExportLongCounterMetricSuccessfully() {
    String meterExporterEndpoint = "http://localhost:" + server.httpPort() + HTTP_ENDPOINT_PATH;
    Map<String, String> properties = new HashMap<>();
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_ENABLED, TRUE.toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_ENDPOINT, meterExporterEndpoint);
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_TYPE, HTTP.toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_INTERVAL, METER_EXPORTER_INTERVAL);

    MeterExporterConfiguration configuration = getMeterExporterConfiguration(properties);
    OpenTelemetryMeterExporterFactory openTelemetryMeterExporterFactory = new TestOpenTelemetryMeterExporterFactory();
    openTelemetryMeterExporter = openTelemetryMeterExporterFactory.getMeterExporter(configuration);

    openTelemetryMeterExporter.registerMeterToExport(meter);
    openTelemetryMeterExporter.enableExport(longCounter);
    longCounter.add(4);

    PollingProber prober = new PollingProber(TIMEOUT_MILLIS, POLL_DELAY_MILLIS);
    prober.check(new JUnitProbe() {

      @Override
      protected boolean test() {
        return getMetricsByCounterName(server.getMetrics(), longCounter.getName()).size() >= 1;
      }

      @Override
      public String describeFailure() {
        return "The expected amount of metrics was not captured.";
      }
    });
  }

  @Test
  public void configuredGrpcInsecureExporterShouldExportLongCounterMetricSuccessfully() {
    String meterExporterHeaders = "{\"Header\": \"Header Value\"}";
    String meterExporterCompressionType = "gzip";
    String meterExporterEndpoint = "http://localhost:" + server.httpPort() + GRPC_ENDPOINT_PATH;

    Map<String, String> properties = new HashMap<>();
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_ENABLED, TRUE.toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_TYPE, GRPC.toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_ENDPOINT, meterExporterEndpoint);
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_HEADERS, meterExporterHeaders);
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_COMPRESSION_TYPE, meterExporterCompressionType);
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_INTERVAL, METER_EXPORTER_INTERVAL);

    MeterExporterConfiguration configuration = getMeterExporterConfiguration(properties);
    OpenTelemetryMeterExporterFactory openTelemetryMeterExporterFactory = new TestOpenTelemetryMeterExporterFactory();
    openTelemetryMeterExporter = openTelemetryMeterExporterFactory.getMeterExporter(configuration);

    openTelemetryMeterExporter.registerMeterToExport(meter);
    openTelemetryMeterExporter.enableExport(longCounter);
    longCounter.add(4);

    PollingProber prober = new PollingProber(TIMEOUT_MILLIS, POLL_DELAY_MILLIS);
    prober.check(new JUnitProbe() {

      @Override
      protected boolean test() {
        return getMetricsByCounterName(server.getMetrics(), longCounter.getName()).size() >= 1;
      }

      @Override
      public String describeFailure() {
        return "The expected amount of metrics was not captured.";
      }
    });
  }

  @Test
  public void configuredHttpInsecureExporterShouldExportLongCounterMetricSuccessfully() {
    String meterExporterEndpoint = "http://localhost:" + server.httpPort() + HTTP_GZIP_ENDPOINT_PATH;
    String meterExporterHeaders = "{\"Header\": \"Header Value\"}";
    String meterExporterCompressionType = "gzip";

    Map<String, String> properties = new HashMap<>();
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_ENABLED, TRUE.toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_TYPE, HTTP.toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_HEADERS, meterExporterHeaders);
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_ENDPOINT, meterExporterEndpoint);
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_COMPRESSION_TYPE, meterExporterCompressionType);
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_INTERVAL, METER_EXPORTER_INTERVAL);

    MeterExporterConfiguration configuration = getMeterExporterConfiguration(properties);
    OpenTelemetryMeterExporterFactory openTelemetryMeterExporterFactory = new TestOpenTelemetryMeterExporterFactory();
    openTelemetryMeterExporter = openTelemetryMeterExporterFactory.getMeterExporter(configuration);

    openTelemetryMeterExporter.registerMeterToExport(meter);
    openTelemetryMeterExporter.enableExport(longCounter);
    longCounter.add(4);

    PollingProber prober = new PollingProber(TIMEOUT_MILLIS, POLL_DELAY_MILLIS);
    prober.check(new JUnitProbe() {

      @Override
      protected boolean test() {
        return getMetricsByCounterName(server.getMetrics(), longCounter.getName()).size() >= 1;
      }

      @Override
      public String describeFailure() {
        return "The expected amount of metrics was not captured.";
      }
    });
  }

  @Test
  public void configuredGrpcSecureExporterShouldExportLongCounterMetricSuccessfully() {
    String meterExporterHeaders = "{\"Header\": \"Header Value\"}";
    String meterExporterCompressionType = "gzip";
    String meterExporterEndpoint = "http://localhost:" + server.httpPort() + GRPC_ENDPOINT_PATH;

    Map<String, String> properties = new HashMap<>();
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_ENABLED, TRUE.toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_TYPE, GRPC.toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_TLS_ENABLED, TRUE.toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_KEY_FILE_LOCATION, clientTls.privateKeyFile().toPath().toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_CERT_FILE_LOCATION, clientTls.certificateFile().toPath().toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_CA_FILE_LOCATION, serverTls.certificateFile().toPath().toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_HEADERS, meterExporterHeaders);
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_ENDPOINT, meterExporterEndpoint);
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_COMPRESSION_TYPE, meterExporterCompressionType);
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_INTERVAL, METER_EXPORTER_INTERVAL);

    MeterExporterConfiguration configuration = getMeterExporterConfiguration(properties);
    OpenTelemetryMeterExporterFactory openTelemetryMeterExporterFactory = new TestOpenTelemetryMeterExporterFactory();
    openTelemetryMeterExporter = openTelemetryMeterExporterFactory.getMeterExporter(configuration);

    openTelemetryMeterExporter.registerMeterToExport(meter);
    openTelemetryMeterExporter.enableExport(longCounter);
    longCounter.add(4);

    PollingProber prober = new PollingProber(TIMEOUT_MILLIS, POLL_DELAY_MILLIS);
    prober.check(new JUnitProbe() {

      @Override
      protected boolean test() {
        return server.getMetrics().size() >= 1;
      }

      @Override
      public String describeFailure() {
        return "The expected amount of metrics was not captured.";
      }
    });
  }

  @Test
  public void configuredHttpSecureExporterShouldExportLongCounterMetricSuccessfully() {
    String meterExporterEndpoint = "http://localhost:" + server.httpPort() + HTTP_GZIP_ENDPOINT_PATH;
    String meterExporterHeaders = "{\"Header\": \"Header Value\"}";
    String meterExporterCompressionType = "gzip";

    Map<String, String> properties = new HashMap<>();
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_ENABLED, TRUE.toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_TYPE, HTTP.toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_TLS_ENABLED, TRUE.toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_KEY_FILE_LOCATION, clientTls.privateKeyFile().toPath().toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_CERT_FILE_LOCATION, clientTls.certificateFile().toPath().toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_CA_FILE_LOCATION, serverTls.certificateFile().toPath().toString());
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_HEADERS, meterExporterHeaders);
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_ENDPOINT, meterExporterEndpoint);
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_COMPRESSION_TYPE, meterExporterCompressionType);
    properties.put(MULE_OPEN_TELEMETRY_METER_EXPORTER_INTERVAL, METER_EXPORTER_INTERVAL);

    MeterExporterConfiguration configuration = getMeterExporterConfiguration(properties);
    OpenTelemetryMeterExporterFactory openTelemetryMeterExporterFactory = new TestOpenTelemetryMeterExporterFactory();
    openTelemetryMeterExporter = openTelemetryMeterExporterFactory.getMeterExporter(configuration);

    openTelemetryMeterExporter.registerMeterToExport(meter);
    openTelemetryMeterExporter.enableExport(longCounter);
    longCounter.add(4);

    PollingProber prober = new PollingProber(TIMEOUT_MILLIS, POLL_DELAY_MILLIS);
    prober.check(new JUnitProbe() {

      @Override
      protected boolean test() {
        return server.getMetrics().size() >= 1;
      }

      @Override
      public String describeFailure() {
        return "The expected amount of metrics was not captured.";
      }
    });
  }

  @NotNull
  private static OpenTelemetryAutoConfigurableMeterExporterConfiguration getMeterExporterConfiguration(Map<String, String> properties) {
    return new OpenTelemetryAutoConfigurableMeterExporterConfiguration(new TestMeterExporterConfiguration(properties));
  }

  private List<TestExportedMeter> getMetricsByCounterName(List<TestExportedMeter> metrics, String metricName) {
    return metrics.stream().filter(metric -> metric.getName().equals(metricName)).collect(Collectors.toList());
  }
}
