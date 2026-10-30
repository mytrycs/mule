/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA module metadata correction Copyright (c) 2026 ZerNova. CPAL-1.0. */
/**
 * Bootstrap API for the Mule Container.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.boot.api {

  exports com.zernova.runtime.module.boot.api;
  exports com.zernova.runtime.module.reboot.api;

  // This directs some exports to modules that are loaded in the container layer. Those are used for compile-time validation and
  // then directed programmatically by JpmsUtils
  exports com.zernova.runtime.module.boot.internal to
      com.zernova.boot,
      com.zernova.boot.tanuki,
      com.zernova.runtime.launcher, // container layer!
      com.mulesoft.mule.boot,
      com.mulesoft.mule.runtime.plugin; // container layer!

  // Needed by the BootModuleLayerValidationBootstrapConfigurer and for creating the container ClassLoader
  requires com.zernova.runtime.jpms.utils;

  // Needed by the ZernovaLog4jConfigurer
  requires static com.zernova.runtime.boot.log4j;

  // Needed by the SLF4JBridgeHandlerBootstrapConfigurer,
  requires jul.to.slf4j;
  // but also to make the logging modules available from the boot layer
  requires static com.zernova.runtime.logging;

  // Public wrapper signatures use CommandLine; consumers must read its module.
  requires transitive org.apache.commons.cli;

  uses com.zernova.runtime.module.boot.api.ZernovaContainerProvider;

  // Required to programmatically propagate accessibility by JpmsUtils
  opens com.zernova.runtime.module.boot.internal to
      com.zernova.runtime.jpms.utils;

}