/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/**
 * A Mule module for connectivity using TLS/SSL authentication.
 *
 * @moduleGraph
 * @since 4.5
 */
module com.zernova.runtime.tls {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.container;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.dsl.api;
  requires com.zernova.runtime.extension.model;
  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.spring.config;

  // QName
  requires java.xml;

  requires com.google.common;
  // needed because byte-buddy generated classes for this module need to use spring SmartFactoryBean
  requires spring.beans;

  exports com.zernova.runtime.module.tls.api;
  exports com.zernova.runtime.module.tls.api.socket;

  exports com.zernova.runtime.module.tls.internal.config to
      com.zernova.runtime.spring.config,
      spring.beans;

  exports com.zernova.runtime.module.tls.internal.revocation to
      spring.beans;

  exports com.zernova.runtime.module.tls.internal.util to
      com.zernova.runtime.launcher;

  provides com.zernova.runtime.api.tls.AbstractTlsContextFactoryBuilderFactory with
      com.zernova.runtime.module.tls.api.DefaultTlsContextFactoryBuilderFactory;

  provides com.zernova.runtime.extension.api.provider.RuntimeExtensionModelProvider with
      com.zernova.runtime.module.tls.api.extension.TlsRuntimeExtensionModelProvider;

  provides com.zernova.runtime.dsl.api.component.ComponentBuildingDefinitionProvider with
      com.zernova.runtime.module.tls.internal.config.TlsComponentBuildingDefinitionProvider;

  provides com.zernova.runtime.dsl.api.xml.XmlNamespaceInfoProvider with
      com.zernova.runtime.module.tls.internal.config.TlsXmlNamespaceInfoProvider;

  opens com.zernova.runtime.module.tls.internal.config to
      net.bytebuddy;

}