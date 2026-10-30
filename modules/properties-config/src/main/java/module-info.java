/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/**
 * Properties Config.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.properties.config {

  requires com.zernova.runtime.api;
  requires transitive com.zernova.runtime.properties.api;
  requires com.zernova.runtime.dsl.api;
  requires com.zernova.runtime.artifact.ast;
  requires com.zernova.runtime.core;

  requires com.google.common;
  requires org.yaml.snakeyaml;
  requires org.vibur.objectpool;

  // QName used to process annotations from Mule DSL
  requires java.xml;

  exports com.zernova.runtime.config.api.properties;

  provides com.zernova.runtime.properties.api.ConfigurationPropertiesProviderFactory with
      com.zernova.runtime.config.internal.model.dsl.properties.DefaultConfigurationPropertiesProviderFactory;

  provides com.zernova.runtime.properties.api.DefaultConfigurationPropertiesProviderFactory with
      com.zernova.runtime.config.internal.model.dsl.properties.DefaultInitialisableConfigurationPropertiesProviderFactory;

  exports com.zernova.runtime.config.internal.model.dsl to
      com.zernova.runtime.metrics.exporter.configuration.impl,
      com.zernova.runtime.tracer.configuration.impl,
      com.zernova.runtime.tracer.exporter.config.impl,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.extensions.xml.support;
  exports com.zernova.runtime.config.internal.model.dsl.config to
      com.zernova.runtime.metrics.exporter.configuration.impl,
      com.zernova.runtime.tracer.configuration.impl,
      com.zernova.runtime.tracer.exporter.config.impl,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.extensions.xml.support,
      com.zernova.runtime.deployment,
      spring.beans;
  exports com.zernova.runtime.config.internal.model.properties to
      com.zernova.runtime.spring.config;

  opens com.zernova.runtime.config.internal.model.dsl.config to
      spring.core;

}
