/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Test Runner for Mule Integration Tests.
 *
 * @moduleGraph
 * @since 4.5
 */
module com.zernova.test.runner {

  requires com.zernova.runtime.api;
  requires com.zernova.sdk.api;
  requires com.zernova.sdk.compatibility.api;
  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.extension.model;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.jpms.utils;
  requires com.zernova.runtime.container;
  requires com.zernova.runtime.service;
  requires com.zernova.runtime.artifact;
  requires com.zernova.runtime.artifact.activation;
  requires com.zernova.runtime.deployment.model;
  requires com.zernova.runtime.extensions.support;
  requires com.zernova.runtime.manifest;

  requires com.zernova.runtime.maven.client.api;
  requires com.zernova.runtime.maven.pom.parser.api;
  requires com.zernova.runtime.maven.client.impl;

  requires java.management;
  requires jdk.management;

  requires com.google.common;
  requires org.apache.commons.codec;
  requires org.apache.commons.collections4;
  requires org.apache.commons.io;
  requires org.apache.commons.lang3;
  requires semver4j;
  requires org.reflections;
  // TODO: MULE-19762 remove once forward compatibility is finished
  requires org.apache.maven.resolver;
  requires org.apache.maven.resolver.util;
  requires net.bytebuddy;

  requires junit;

  exports com.zernova.test.runner;

}
