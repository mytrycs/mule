/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/**
 * Mule service module. Provides services support to Mule container.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.service {

  requires transitive com.zernova.runtime.api;
  requires transitive com.zernova.runtime.artifact;
  requires com.zernova.runtime.container;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.jpms.utils;
  requires com.zernova.runtime.metadata.model.java;

  requires com.google.common;
  requires org.apache.commons.io;
  requires org.apache.commons.lang3;
  requires org.reflections;

  exports com.zernova.runtime.module.service.api.artifact;
  exports com.zernova.runtime.module.service.api.discoverer;
  exports com.zernova.runtime.module.service.api.manager;

  exports com.zernova.runtime.module.service.internal.artifact to
      com.zernova.test.runner;
  exports com.zernova.runtime.module.service.internal.discoverer to
      com.zernova.runtime.artifact.activation,
      com.zernova.runtime.deployment.model.impl;
  exports com.zernova.runtime.module.service.internal.manager to
      com.zernova.runtime.spring.config,
      com.zernova.runtime.deployment.model.impl;
  exports com.zernova.runtime.module.service.internal.util to
      com.zernova.runtime.spring.config;

}
