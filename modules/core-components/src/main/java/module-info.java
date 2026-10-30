/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Core components implementations for the Mule Runtime.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.core.components {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.dsl.api;
  requires com.zernova.runtime.errors;
  requires com.zernova.runtime.extension.model;
  requires com.zernova.runtime.extensions.mimeTypes;
  requires com.zernova.runtime.tracer.api;
  requires com.zernova.runtime.tracer.customization.api;

  // javax.activation
  requires jakarta.activation;
  // QName
  requires java.xml;
  requires reactor.core;
  requires org.apache.commons.lang3;
  requires org.reactivestreams;

  exports com.zernova.runtime.core.internal.routing to
      com.zernova.runtime.spring.config,
      com.mulesoft.mule.runtime.batch,
      spring.beans;
  exports com.zernova.runtime.core.internal.routing.forkjoin to
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.core.internal.source.scheduler to
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.core.internal.processor to
      com.zernova.runtime.deployment,
      com.zernova.runtime.spring.config,
      spring.beans,
      com.mulesoft.mule.runtime.kryo;
  exports com.zernova.runtime.core.internal.processor.simple to
      com.zernova.runtime.core,
      com.zernova.runtime.spring.config,
      spring.beans;

  opens com.zernova.runtime.core.internal.routing to
      com.zernova.runtime.core,
      spring.core;
  opens com.zernova.runtime.core.internal.source.scheduler to
      com.zernova.runtime.core,
      spring.core;
  opens com.zernova.runtime.core.internal.processor to
      com.zernova.runtime.core,
      spring.core;

}
