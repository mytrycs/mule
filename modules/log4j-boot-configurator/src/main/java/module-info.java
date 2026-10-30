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
module com.zernova.runtime.boot.log4j {

  // Exported.
  exports com.zernova.runtime.module.log4j.boot.api;

  requires com.zernova.runtime.logging;

  // Third party modules
  requires com.lmax.disruptor;
  requires org.apache.logging.log4j.core;

  requires transitive org.apache.logging.log4j;
  requires org.slf4j;
}
