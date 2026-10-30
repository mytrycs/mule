/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/**
 * Mule Log4j Configurator Module.
 *
 * @moduleGraph
 * @since 4.5
 */
module com.zernova.runtime.log4j {

  // Mule modules
  requires com.zernova.runtime.api;
  requires com.zernova.runtime.artifact;
  requires com.zernova.runtime.artifact.activation;
  requires com.zernova.runtime.boot.log4j;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.deployment.model;

  // Third party modules
  requires com.github.benmanes.caffeine;
  requires com.google.common;
  requires com.lmax.disruptor;
  requires it.unimi.dsi.fastutil;
  requires org.apache.commons.lang3;
  requires org.apache.logging.log4j.core;
  requires org.reflections;

  requires org.apache.logging.log4j;

  exports com.zernova.runtime.module.log4j.internal to
      com.zernova.runtime.launcher, com.zernova.test.infrastructure;

  provides com.zernova.runtime.module.artifact.api.classloader.BlockingLoggerResolutionClassRegistry with
      com.zernova.runtime.module.log4j.internal.Log4JBlockingLoggerResolutionClassRegistry;

}
