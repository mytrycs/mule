/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Provides container artifacts deployment functionality
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.deployment {

  requires transitive com.zernova.runtime.api;
  requires com.zernova.runtime.artifact;
  requires com.zernova.runtime.artifact.activation;
  requires com.zernova.runtime.artifact.ast;
  requires com.zernova.runtime.artifact.ast.serialization;
  requires com.zernova.runtime.artifact.ast.xmlParser;
  requires com.zernova.runtime.artifact.declaration;
  requires com.zernova.runtime.container;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.core.components;
  requires com.zernova.runtime.deployment.model;
  requires com.zernova.runtime.deployment.model.impl;
  requires com.zernova.runtime.extension.model;
  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.extensions.spring.support;
  requires com.zernova.runtime.global.config;
  requires com.zernova.runtime.maven.client.api;
  requires com.zernova.runtime.policy.api;
  requires com.zernova.runtime.properties.config;
  requires com.zernova.runtime.spring.config;
  requires com.zernova.sdk.api;

  // package java.beans package is used
  requires java.desktop;
  requires com.google.common;
  requires org.apache.commons.io;
  requires org.apache.commons.lang3;
  requires spring.core;

  exports com.zernova.runtime.module.deployment.api;
  exports com.zernova.runtime.module.deployment.internal.processor to
      com.zernova.runtime.launcher;

  exports com.zernova.runtime.module.deployment.internal.singleapp to
      com.zernova.runtime.launcher,
      spring.beans,
      com.zernova.runtime.deployment.test;
  exports com.zernova.runtime.module.deployment.internal to
      com.zernova.runtime.deployment.test,
      com.zernova.runtime.launcher,
      spring.beans;

  opens com.zernova.runtime.module.deployment.internal.singleapp to
      spring.core,
      com.zernova.runtime.deployment.test;
  opens com.zernova.runtime.module.deployment.internal to
      com.zernova.runtime.deployment.test,
      spring.core;



}
