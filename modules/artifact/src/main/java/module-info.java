/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/**
 * Mule Artifact Module.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.artifact {

  requires transitive com.zernova.runtime.api;
  requires com.zernova.sdk.api;
  requires com.zernova.runtime.artifact.declaration;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.jar.handling.utils;
  requires com.zernova.runtime.manifest;

  requires java.desktop;
  requires java.management;
  requires java.sql;

  requires com.github.benmanes.caffeine;
  requires org.apache.commons.io;
  requires org.apache.commons.lang3;
  requires semver4j;

  exports com.zernova.runtime.module.artifact.api;
  exports com.zernova.runtime.module.artifact.api.classloader;
  exports com.zernova.runtime.module.artifact.api.classloader.exception;
  exports com.zernova.runtime.module.artifact.api.classloader.net;
  exports com.zernova.runtime.module.artifact.api.descriptor;
  exports com.zernova.runtime.module.artifact.api.plugin;
  exports com.zernova.runtime.module.artifact.api.serializer;

  exports com.zernova.module.artifact.classloader to
      com.zernova.runtime.extensions.support;
  exports com.zernova.runtime.module.artifact.internal.classloader to
      com.zernova.runtime.artifact.activation,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.deployment.model,
      com.zernova.runtime.deployment.model.impl,
      com.zernova.runtime.launcher,
      com.zernova.test.runner;
  exports com.zernova.runtime.module.artifact.internal.util to
      com.zernova.runtime.container,
      com.zernova.runtime.artifact.activation,
      com.zernova.runtime.deployment,
      com.zernova.runtime.deployment.model,
      com.zernova.runtime.deployment.model.impl,
      com.zernova.test.runner;

  uses com.zernova.runtime.module.artifact.api.classloader.BlockingLoggerResolutionClassRegistry;
  uses com.zernova.runtime.module.artifact.api.descriptor.ClassLoaderConfigurationLoader;
  uses com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptorLoader;

}
