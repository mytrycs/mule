/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/**
 * Definitions for Mule Metrics.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.metrics.internal.impl {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.metrics.api;
  requires com.zernova.runtime.metrics.exporter.api;
  requires com.zernova.runtime.metrics.exporter.configuration.api;

  exports com.zernova.runtime.metrics.impl to
      com.zernova.runtime.spring.config,
      spring.beans;

  opens com.zernova.runtime.metrics.impl to
      spring.core;
    exports com.zernova.runtime.metrics.impl.meter.error to com.zernova.runtime.spring.config, spring.beans;
    opens com.zernova.runtime.metrics.impl.meter.error to spring.core;

}
