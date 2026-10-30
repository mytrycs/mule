/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/**
 * Mule Embedded Implementation.
 *
 * @moduleGraph
 * @since 4.5
 */
module com.zernova.distribution.embedded.impl {

  requires transitive com.zernova.runtime.launcher;
  requires transitive com.zernova.runtime.api;

  requires com.zernova.runtime.core;
  requires com.zernova.runtime.artifact;
  requires com.zernova.runtime.container;
  requires com.zernova.runtime.deployment;
  requires com.zernova.runtime.log4j;

  requires com.zernova.runtime.embedded.api;
  requires com.zernova.runtime.embedded.commons;

  requires org.apache.commons.io;
  requires zip4j;

  provides com.zernova.runtime.module.embedded.api.controller.EmbeddedControllerProvider
      with com.zernova.runtime.module.embedded.impl.DefaultEmbeddedControllerProvider;

}
