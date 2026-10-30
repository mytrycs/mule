/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Provides container artifact related functionality.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.container {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.jar.handling.utils;
  requires com.zernova.runtime.artifact;
  requires com.zernova.runtime.jpms.utils;

  requires org.apache.commons.io;
  requires org.apache.commons.lang3;
  requires com.google.common;

  exports com.zernova.runtime.container.api;
  exports com.zernova.runtime.container.api.discoverer;

  exports com.zernova.runtime.container.internal to
      com.zernova.runtime.artifact.activation,
      com.zernova.runtime.deployment,
      com.zernova.runtime.deployment.model,
      com.zernova.runtime.deployment.model.impl,
      com.zernova.runtime.service,
      com.zernova.runtime.spring.config,
      com.zernova.test.runner;
}
