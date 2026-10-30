/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/**
 * Implementation for the internal tracer.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.tracer.internal.impl {

  requires com.zernova.runtime.artifact.ast;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.tracer.customization.api;
  requires com.zernova.runtime.tracer.exporter.api;
  requires com.zernova.runtime.tracer.exporter.configuration.api;

  requires org.apache.commons.lang3;
  requires com.google.common;

  exports com.zernova.runtime.tracer.impl to
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.tracer.impl.span to
      com.mulesoft.mule.runtime.batch,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.tracer.exporter.impl;
  exports com.zernova.runtime.tracer.impl.span.factory to
      com.zernova.runtime.spring.config,
      spring.beans;

  opens com.zernova.runtime.tracer.impl to
      spring.core;
  opens com.zernova.runtime.tracer.impl.span.factory to
      spring.core;

}
