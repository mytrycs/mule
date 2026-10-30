/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

import com.zernova.runtime.extension.api.provider.RuntimeExtensionModelProvider;

/**
 * Default implementation of the Mule Extension XML API.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.extensions.xml.support {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.metadata.model.api;
  requires com.zernova.runtime.metadata.model.java;
  requires com.zernova.runtime.metadata.model.persistence;
  requires com.zernova.runtime.metadata.model.catalog;
  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.dsl.api;
  requires com.zernova.runtime.artifact.ast;
  requires com.zernova.runtime.artifact.ast.xmlParser;
  requires com.zernova.runtime.properties.api;
  requires com.zernova.runtime.extension.model;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.errors;
  requires com.zernova.runtime.extensions.support;
  requires com.zernova.runtime.extensions.spring.support;
  requires com.zernova.runtime.properties.config;
  requires com.zernova.runtime.spring.config;

  requires java.xml;

  requires com.google.common;
  requires com.google.gson;
  requires org.apache.commons.beanutils;
  requires org.apache.commons.io;
  requires org.apache.commons.lang3;
  requires org.jgrapht.core;
  requires net.bytebuddy;
  requires org.vibur.objectpool;

  provides com.zernova.runtime.config.internal.model.ApplicationModelAstPostProcessor with
      com.zernova.runtime.extension.internal.ast.MacroExpansionAstPostProcessor;
  provides RuntimeExtensionModelProvider with
      com.zernova.runtime.extension.api.extension.XmlSdk1RuntimeExtensionModelProvider;
  provides com.zernova.runtime.dsl.api.component.ComponentBuildingDefinitionProvider with
      com.zernova.runtime.extension.internal.config.dsl.XmlExtensionBuildingDefinitionProvider;
  provides com.zernova.runtime.dsl.api.xml.XmlNamespaceInfoProvider with
      com.zernova.runtime.extension.internal.config.dsl.xml.ModuleXmlNamespaceInfoProvider;
  provides com.zernova.runtime.extension.api.loader.ExtensionModelLoaderProvider with
      com.zernova.runtime.extension.api.loader.xml.XmlExtensionModelLoaderProvider;

  exports com.zernova.runtime.extension.internal.config.dsl to
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.extension.internal.factories to
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.extension.internal.processor to
      spring.beans;

  opens com.zernova.runtime.extension.internal.config.dsl to
      net.bytebuddy;
  opens com.zernova.runtime.extension.internal.factories to
      net.bytebuddy,
      spring.core;
  opens com.zernova.runtime.extension.internal.processor to
      spring.core;
  exports com.zernova.runtime.extension.internal.loader.xml to
      com.zernova.runtime.deployment.test;

}
