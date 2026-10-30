/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/**
 * Definitions for Mule Metrics Exporter Implementation.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.metrics.exporter.impl {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.artifact.ast;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.metrics.exporter.api;
  requires com.zernova.runtime.metrics.exporter.configuration.api;
  requires com.zernova.runtime.metrics.exporter.configuration.impl;
  requires com.zernova.runtime.metrics.api;

  requires com.google.gson;
  requires io.opentelemetry.api;
  requires io.opentelemetry.exporter.otlp;
  requires io.opentelemetry.sdk.common;
  requires io.opentelemetry.sdk.metrics;
  requires io.opentelemetry.sdk.testing;
  requires org.apache.commons.lang3;
  requires io.opentelemetry.exporter.logging;

  exports com.zernova.runtime.metrics.exporter.impl to
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.metrics.exporter.impl.optel.config to
      com.zernova.runtime.spring.config,
      spring.beans;

  opens com.zernova.runtime.metrics.exporter.impl to
      spring.core;
  opens com.zernova.runtime.metrics.exporter.impl.optel.config to
      spring.core;

}
