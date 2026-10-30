/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Implementation for the tracer exporter
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.tracer.exporter.impl {

  requires com.zernova.runtime.artifact.ast;

  requires com.zernova.runtime.tracer.api;
  requires com.zernova.runtime.tracer.exporter.api;
  requires com.zernova.runtime.tracer.exporter.configuration.api;
  requires com.zernova.runtime.tracer.exporter.config.impl;
  requires com.zernova.runtime.tracer.internal.impl;
  requires com.zernova.runtime.core;

  requires io.opentelemetry.api;
  requires io.opentelemetry.context;
  requires io.opentelemetry.exporter.internal;
  requires io.opentelemetry.exporter.otlp;
  requires io.opentelemetry.sdk.common;
  requires io.opentelemetry.sdk.metrics;
  requires io.opentelemetry.sdk.trace;

  requires com.google.gson;
  requires org.apache.commons.lang3;

  exports com.zernova.runtime.tracer.exporter.impl to
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.tracer.exporter.impl.optel.config to
      com.zernova.runtime.spring.config,
      spring.beans;

  opens com.zernova.runtime.tracer.exporter.impl to
      spring.core;
  opens com.zernova.runtime.tracer.exporter.impl.optel.config to
      spring.core;

}
