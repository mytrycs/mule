/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/**
 * Implementation for setting configuration for tracing levels and overrides.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.tracer.customization.impl {

  requires com.zernova.runtime.core;
  requires com.zernova.runtime.tracer.api;
  requires com.zernova.runtime.tracer.customization.api;
  requires com.zernova.runtime.tracer.configuration.api;

  requires org.apache.commons.lang3;

  exports com.zernova.runtime.tracer.customization.impl.provider to
      com.zernova.runtime.spring.config,
      spring.beans;

}
