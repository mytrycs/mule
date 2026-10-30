/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Support for extensions to be configured through a mule config file backed by the Spring framework.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.extensions.spring.support {

  requires com.zernova.sdk.api;
  requires com.zernova.runtime.api;
  requires com.zernova.runtime.metadata.model.api;
  requires com.zernova.runtime.metadata.model.java;
  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.dsl.api;
  requires com.zernova.runtime.tracer.api;
  requires com.zernova.runtime.tracer.customization.api;
  requires com.zernova.runtime.extension.model;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.extensions.support;
  requires com.zernova.runtime.artifact;
  requires com.zernova.runtime.artifact.activation;
  requires com.zernova.runtime.deployment.model;
  requires com.zernova.runtime.spring.config;

  requires java.compiler;

  requires jakarta.xml.bind;

  requires com.google.common;
  requires org.apache.commons.lang3;
  requires org.dom4j;
  requires org.jsoup;
  requires org.reflections;

  provides com.zernova.runtime.dsl.api.component.ComponentBuildingDefinitionProvider with
      com.zernova.runtime.module.extension.internal.config.dsl.DefaultExtensionBuildingDefinitionProvider;
  provides com.zernova.runtime.extension.api.dsl.syntax.resources.spi.ExtensionSchemaGenerator with
      com.zernova.runtime.module.extension.internal.capability.xml.schema.DefaultExtensionSchemaGenerator;
  provides com.zernova.runtime.extension.api.resources.spi.GeneratedResourceFactory with
      com.zernova.runtime.module.extension.internal.resources.ZernovaPluginDescriptorGenerator;

  exports com.zernova.runtime.module.extension.api.resources;

  exports com.zernova.runtime.module.extension.internal.capability.xml.description to
      com.zernova.runtime.ast.extension;
  exports com.zernova.runtime.module.extension.internal.config.dsl to
      com.zernova.runtime.extensions.xml.support,
      spring.beans;
  exports com.zernova.runtime.module.extension.internal.config.dsl.connection to
      com.zernova.runtime.spring.config,
      com.zernova.runtime.extensions.xml.support,
      spring.beans;
  exports com.zernova.runtime.module.extension.internal.config.dsl.config to
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.module.extension.internal.config.dsl.construct to
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.module.extension.internal.config.dsl.operation to
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.module.extension.internal.config.dsl.parameter to
      com.zernova.runtime.extensions.xml.support,
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.module.extension.internal.config.dsl.source to
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.module.extension.internal.resources to
      com.zernova.runtime.ast.extension;
  exports com.zernova.runtime.module.extension.internal.resources.validator to
      com.zernova.runtime.ast.extension;

  opens com.zernova.runtime.module.extension.internal.capability.xml.schema.model to
      jakarta.xml.bind;
  opens com.zernova.runtime.module.extension.internal.config.dsl to
      spring.core;
  opens com.zernova.runtime.module.extension.internal.config.dsl.config to
      net.bytebuddy,
      spring.core;
  opens com.zernova.runtime.module.extension.internal.config.dsl.connection to
      net.bytebuddy,
      spring.core;
  opens com.zernova.runtime.module.extension.internal.config.dsl.construct to
      net.bytebuddy,
      spring.core;
  opens com.zernova.runtime.module.extension.internal.config.dsl.operation to
      net.bytebuddy,
      spring.core;
  opens com.zernova.runtime.module.extension.internal.config.dsl.parameter to
      net.bytebuddy,
      spring.core;
  opens com.zernova.runtime.module.extension.internal.config.dsl.source to
      net.bytebuddy;

}
