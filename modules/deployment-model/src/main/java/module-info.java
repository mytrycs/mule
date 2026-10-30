/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Provides descriptors and class loaders for deployment artifacts.
 *
 * @moduleGraph
 * @since 4.6
 * @deprecated Use {@link com.zernova.runtime.artifact.activation} instead.
 */
@Deprecated
module com.zernova.runtime.deployment.model {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.dsl.api;
  requires com.zernova.runtime.artifact.ast;
  requires com.zernova.runtime.artifact.declaration;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.artifact;
  requires com.zernova.runtime.artifact.activation;
  requires com.zernova.runtime.container;

  requires org.apache.commons.lang3;

  exports com.zernova.runtime.deployment.model.api;
  exports com.zernova.runtime.deployment.model.api.application;
  exports com.zernova.runtime.deployment.model.api.artifact;
  exports com.zernova.runtime.deployment.model.api.builder;
  exports com.zernova.runtime.deployment.model.api.domain;
  exports com.zernova.runtime.deployment.model.api.plugin;
  exports com.zernova.runtime.deployment.model.api.plugin.resolver;
  exports com.zernova.runtime.deployment.model.api.policy;

  exports com.zernova.runtime.deployment.model.internal to
      com.zernova.runtime.log4j,
      com.zernova.runtime.deployment.model.impl,
      com.zernova.test.runner;
  exports com.zernova.runtime.deployment.model.internal.artifact to
      com.zernova.runtime.deployment,
      com.zernova.runtime.deployment.model.impl,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.log4j;
  exports com.zernova.runtime.deployment.model.internal.policy to
      com.zernova.runtime.deployment.model.impl;

  uses com.zernova.runtime.deployment.model.api.artifact.ArtifactDescriptorFactoryProvider;

}