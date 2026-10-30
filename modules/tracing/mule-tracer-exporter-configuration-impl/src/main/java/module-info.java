/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/**
 * Implementation for the internal exporter configuration.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.tracer.exporter.config.impl {

  requires com.zernova.runtime.container;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.module.observability;
  requires com.zernova.runtime.properties.config;
  requires com.zernova.runtime.tracer.api;
  requires com.zernova.runtime.tracer.common;
  requires com.zernova.runtime.tracer.exporter.api;
  requires com.zernova.runtime.tracer.exporter.configuration.api;

  requires com.fasterxml.jackson.databind;

  exports com.zernova.runtime.tracer.exporter.config.impl to
      com.zernova.runtime.tracer.exporter.impl;

}
