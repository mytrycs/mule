/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
import com.zernova.runtime.api.config.custom.ServiceConfigurator;
import com.zernova.runtime.module.launcher.privileged.ContainerServiceProvider;
import com.zernova.runtime.module.tooling.internal.config.ToolingServicesConfigurator;
import com.zernova.runtime.module.tooling.internal.launcher.ToolingSupportContainerServiceProvider;

/**
 * This modules provides a set of services for tooling applications.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.tooling.support {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.artifact;
  requires com.zernova.runtime.artifact.activation;
  requires com.zernova.runtime.artifact.ast.xmlParser;
  requires com.zernova.runtime.artifact.declaration;
  requires com.zernova.runtime.container;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.deployment;
  requires com.zernova.runtime.deployment.model;
  requires com.zernova.runtime.deployment.model.impl;
  requires com.zernova.runtime.extension.model;
  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.extensions.support;
  requires com.zernova.runtime.global.config;
  requires com.zernova.runtime.launcher;
  requires com.zernova.runtime.license.api;
  requires com.zernova.runtime.manifest;
  requires com.zernova.runtime.maven.client.api;
  requires com.zernova.runtime.maven.pom.parser.api;
  requires com.zernova.runtime.metadata.model.api;
  requires com.zernova.runtime.metadata.model.java;
  requires com.zernova.runtime.metadata.support;
  requires com.zernova.runtime.properties.config;
  requires com.zernova.runtime.repository;
  requires com.zernova.runtime.spring.config;
  requires com.zernova.sdk.api;

  requires com.google.common;
  requires org.apache.commons.io;
  requires org.apache.commons.lang3;
  // Aether
  requires org.apache.maven.resolver;

  exports com.zernova.runtime.module.tooling.api;
  exports com.zernova.runtime.module.tooling.api.artifact;
  exports com.zernova.runtime.module.tooling.api.connectivity;
  exports com.zernova.runtime.module.tooling.internal to
      com.zernova.runtime.launcher;

  exports com.zernova.runtime.module.tooling.internal.data.sample to
      spring.beans;
  exports com.zernova.runtime.module.tooling.internal.config to
      spring.beans;
  exports com.zernova.runtime.module.tooling.internal.connectivity to
      spring.beans;
  exports com.zernova.runtime.module.tooling.internal.metadata.model to
      spring.beans;
  exports com.zernova.runtime.module.tooling.internal.value to
      spring.beans;

  opens com.zernova.runtime.module.tooling.internal.data.sample to
      spring.core;
  opens com.zernova.runtime.module.tooling.internal.config to
      spring.core;
  opens com.zernova.runtime.module.tooling.internal.connectivity to
      spring.core;
  opens com.zernova.runtime.module.tooling.internal.metadata to
      spring.core;
  opens com.zernova.runtime.module.tooling.internal.metadata.cache.lazy to
      spring.core;
  opens com.zernova.runtime.module.tooling.internal.value to
      spring.core;

  provides com.zernova.runtime.config.api.dsl.ArtifactDeclarationXmlSerializer
      with com.zernova.runtime.module.tooling.internal.dsl.model.DefaultArtifactDeclarationXmlSerializer;
  provides com.zernova.runtime.config.api.dsl.model.DslElementModelFactory
      with com.zernova.runtime.module.tooling.internal.dsl.model.DefaultDslElementModelFactory;
  provides ContainerServiceProvider
      with ToolingSupportContainerServiceProvider;
  provides ServiceConfigurator with
      ToolingServicesConfigurator;

}