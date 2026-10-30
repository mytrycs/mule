/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

import com.zernova.runtime.module.launcher.privileged.ContainerServiceProvider;

/**
 * Mule Container Launcher module.
 *
 * @moduleGraph
 * @since 4.5
 */
module com.zernova.runtime.launcher {

  requires com.zernova.boot.api;
  requires com.zernova.runtime.api;
  requires com.zernova.runtime.artifact;
  requires com.zernova.runtime.artifact.activation;
  requires com.zernova.runtime.boot.log4j;
  requires com.zernova.runtime.container;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.deployment;
  requires com.zernova.runtime.deployment.model.impl;
  requires com.zernova.runtime.log4j;
  requires com.zernova.runtime.manifest;
  requires com.zernova.runtime.repository;
  requires com.zernova.runtime.service;
  requires com.zernova.runtime.tls;

  requires org.apache.commons.io;
  requires org.apache.commons.lang3;

  // Embedded uses this
  exports com.zernova.runtime.module.launcher;

  exports com.zernova.runtime.module.launcher.privileged to
      com.zernova.runtime.tooling.support,
      com.zernova.runtime.troubleshooting;

  exports com.zernova.runtime.module.launcher.coreextension to
      com.zernova.runtime.core,
      com.mulesoft.mule.runtime.plugin;

  uses ContainerServiceProvider;

  provides com.zernova.runtime.module.boot.api.ZernovaContainerProvider with
      com.zernova.runtime.module.launcher.LauncherZernovaContainerProvider;

}
