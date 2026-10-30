/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

import com.zernova.runtime.module.launcher.privileged.ContainerServiceProvider;
import com.zernova.runtime.module.troubleshooting.internal.launcher.TroubleshootingContainerServiceProvider;

/**
 * Module containing an injectable interface capable to call certain troubleshooting operations.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.troubleshooting {

  requires com.zernova.runtime.api.annotations;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.container;
  requires com.zernova.runtime.deployment;
  requires com.zernova.runtime.deployment.model;
  requires com.zernova.runtime.deployment.model.impl;
  requires com.zernova.runtime.launcher;
  requires com.zernova.runtime.manifest;

  requires org.apache.commons.lang3;
  requires org.json;
  requires com.google.gson;

  exports com.zernova.runtime.module.troubleshooting.api;

  exports com.zernova.runtime.module.troubleshooting.internal to
      com.zernova.runtime.launcher;

  provides ContainerServiceProvider
      with TroubleshootingContainerServiceProvider;
}
