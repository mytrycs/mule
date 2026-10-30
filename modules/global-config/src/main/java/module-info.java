/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * This module handles the global configuration of the Mule Runtime.
 *
 * @moduleGraph
 * @since 4.5
 */
module com.zernova.runtime.global.config {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.api.annotations;
  requires com.zernova.runtime.container;
  requires com.zernova.runtime.core;
  requires transitive com.zernova.runtime.maven.client.api;

  requires typesafe.config;
  requires everit.json.schema;
  requires org.json;

  exports com.zernova.runtime.globalconfig.api;
  exports com.zernova.runtime.globalconfig.api.cluster;
  exports com.zernova.runtime.globalconfig.api.maven;

}
