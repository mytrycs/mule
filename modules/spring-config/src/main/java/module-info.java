/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/**
 * Mule Builder for use with Spring 4.3.x Namespace based XML configuration.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.spring.config {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.artifact.ast;
  requires com.zernova.runtime.artifact.ast.dependency.graph;
  requires com.zernova.runtime.artifact.ast.xmlParser;
  requires com.zernova.runtime.artifact.declaration;
  requires com.zernova.runtime.dsl.api;
  requires com.zernova.runtime.errors;
  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.metadata.model.api;
  requires com.zernova.runtime.metadata.model.java;
  requires com.zernova.runtime.metadata.model.message;
  requires com.zernova.runtime.properties.api;
  requires com.zernova.sdk.api;

  requires com.zernova.runtime.artifact;
  requires com.zernova.runtime.artifact.activation;
  requires com.zernova.runtime.container;
  requires transitive com.zernova.runtime.core;
  requires transitive com.zernova.runtime.core.components;
  requires com.zernova.runtime.deployment.model;
  requires com.zernova.runtime.extension.model;
  requires com.zernova.runtime.extensions.support;
  requires com.zernova.runtime.featureManagement;
  requires com.zernova.runtime.memory.management;
  requires com.zernova.runtime.metadata.support;
  requires com.zernova.runtime.metrics.api;
  requires com.zernova.runtime.metrics.exporter.api;
  requires com.zernova.runtime.metrics.exporter.impl;
  requires com.zernova.runtime.metrics.internal.impl;
  requires com.zernova.runtime.properties.config;
  requires com.zernova.runtime.service;
  requires com.zernova.runtime.tracer.api;
  requires com.zernova.runtime.tracer.configuration.api;
  requires com.zernova.runtime.tracer.configuration.impl;
  requires com.zernova.runtime.tracer.customization.api;
  requires com.zernova.runtime.tracer.customization.impl;
  requires com.zernova.runtime.tracer.exporter.impl;
  requires com.zernova.runtime.tracer.internal.impl;

  requires spring.beans;
  requires spring.context;
  requires spring.core;

  requires reactor.core;

  requires org.apache.commons.beanutils;
  requires org.apache.commons.lang3;
  requires com.github.benmanes.caffeine;
  requires com.google.common;

  requires org.jgrapht.core;
  requires net.bytebuddy;

  // Still needed for the deprecated properties support
  requires org.yaml.snakeyaml;

  // PropertyDescriptor and PropertyEditor from java.beans
  requires java.desktop;
  requires jakarta.transaction;
  requires com.zernova.runtime.http.support;

  exports com.zernova.runtime.config.api;
  exports com.zernova.runtime.config.api.bean;
  exports com.zernova.runtime.config.api.dsl;
  exports com.zernova.runtime.config.api.dsl.model;
  exports com.zernova.runtime.config.api.dsl.model.properties;
  exports com.zernova.runtime.config.api.dsl.processor;
  exports com.zernova.runtime.config.api.dsl.processor.xml;
  exports com.zernova.runtime.config.api.factories.streaming;

  provides com.zernova.runtime.dsl.api.component.ComponentBuildingDefinitionProvider with
      com.zernova.runtime.config.internal.dsl.model.CoreComponentBuildingDefinitionProvider;

  // Required to build MUnit chains
  exports com.zernova.runtime.config.privileged.dsl.spring;
  exports com.zernova.runtime.config.privileged.spring
      to spring.core;

  exports com.zernova.runtime.config.internal to
      com.zernova.runtime.deployment,
      com.zernova.runtime.deployment.model.impl;
  exports com.zernova.runtime.config.internal.bean to
      spring.beans;
  exports com.zernova.runtime.config.internal.context to
      com.zernova.runtime.deployment,
      spring.beans;
  exports com.zernova.runtime.config.internal.context.lazy to
      com.zernova.runtime.tooling.support;
  exports com.zernova.runtime.config.internal.dsl.processor to
      com.zernova.runtime.core,
      spring.beans;
  exports com.zernova.runtime.config.internal.dsl.spring to
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.extensions.mule.support,
      com.zernova.runtime.extensions.xml.support,
      com.zernova.runtime.tls,
      com.mulesoft.mule.runtime.bti,
      com.mulesoft.mule.runtime.kryo,
      com.mulesoft.mule.runtime.spring.config.ee,
      spring.beans;
  exports com.zernova.runtime.config.internal.el to
      spring.beans;
  exports com.zernova.runtime.config.internal.factories to
      spring.beans;
  exports com.zernova.runtime.config.internal.factories.streaming to
      spring.beans;
  exports com.zernova.runtime.config.internal.lazy to
      spring.beans;
  exports com.zernova.runtime.config.internal.model to
      com.zernova.runtime.extensions.xml.support;
  exports com.zernova.runtime.config.internal.processor to
      spring.beans;

  uses com.zernova.runtime.config.internal.model.ApplicationModelAstPostProcessor;
  uses com.zernova.runtime.config.api.dsl.ArtifactDeclarationXmlSerializer;
  uses com.zernova.runtime.config.api.dsl.model.DslElementModelFactory;
  uses com.zernova.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProviderFactory;

  opens com.zernova.runtime.config.api.dsl to
      net.bytebuddy,
      spring.core;
  opens com.zernova.runtime.config.api.factories.streaming to
      spring.core;
  opens com.zernova.runtime.config.internal.bean to
      spring.core;
  opens com.zernova.runtime.config.internal.context.service to
      com.zernova.runtime.core;
  opens com.zernova.runtime.config.internal.dsl.processor to
      net.bytebuddy,
      spring.core;
  opens com.zernova.runtime.config.internal.dsl.spring to
      net.bytebuddy;
  opens com.zernova.runtime.config.internal.el to
      spring.core;
  opens com.zernova.runtime.config.internal.factories to
      com.zernova.runtime.core,
      net.bytebuddy,
      spring.core;
  opens com.zernova.runtime.config.internal.factories.streaming to
      net.bytebuddy;

}
