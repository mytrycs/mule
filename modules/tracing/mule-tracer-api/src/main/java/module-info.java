/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Definitions for Mule tracer.
 *
 * @moduleGraph
 * @since 4.5
 */
module com.zernova.runtime.tracer.api {

  requires transitive com.zernova.runtime.profiling.api;

  requires com.zernova.runtime.api;

  exports com.zernova.runtime.tracer.api;
  exports com.zernova.runtime.tracer.api.context;
  exports com.zernova.runtime.tracer.api.context.getter;
  exports com.zernova.runtime.tracer.api.sniffer;
  exports com.zernova.runtime.tracer.api.span;
  exports com.zernova.runtime.tracer.api.span.error;
  exports com.zernova.runtime.tracer.api.span.exporter;
  exports com.zernova.runtime.tracer.api.span.info;
  exports com.zernova.runtime.tracer.api.span.validation;
  exports com.zernova.runtime.tracer.api.component;

}
