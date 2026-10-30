/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/**
 * Module that unifies common code between the different observability options provided by the runtime.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.module.observability {

  requires com.zernova.runtime.container;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.properties.config;

  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.dataformat.yaml;

  exports com.zernova.runtime.module.observability to
      com.zernova.runtime.tracer.exporter.config.impl,
      com.zernova.runtime.metrics.exporter.configuration.impl;

}
