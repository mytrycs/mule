/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Extensions API implementation for writing extensions using the Mule language.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.extensions.mule.support {

  requires com.zernova.sdk.api;

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.metadata.model.api;
  requires com.zernova.runtime.errors;
  requires com.zernova.runtime.metadata.model.java;
  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.dsl.api;
  requires com.zernova.runtime.artifact.ast;
  requires com.zernova.runtime.artifact.ast.xmlParser;
  requires com.zernova.runtime.extension.model;
  requires com.zernova.runtime.core;
  // TODO W-18813696: remove this once and for all, we still require it for the DefaultArtifactTypeLoader and some internals of
  // the execution (see ZernovaOperationExecutor)
  requires com.zernova.runtime.extensions.support;
  requires com.zernova.runtime.artifact.activation;
  requires com.zernova.runtime.spring.config;

  requires org.jgrapht.core;

  provides com.zernova.runtime.extension.api.provider.RuntimeExtensionModelProvider
      with com.zernova.runtime.module.extension.mule.api.extension.ZernovaSdkExtensionRuntimeExtensionModelProvider,
      com.zernova.runtime.module.extension.mule.api.extension.OperationDslExtensionModelProvider;
  provides com.zernova.runtime.dsl.api.component.ComponentBuildingDefinitionProvider
      with com.zernova.runtime.module.extension.mule.internal.config.provider.OperationDslBuildingDefinitionProvider;
  provides com.zernova.runtime.dsl.api.xml.XmlNamespaceInfoProvider
      with com.zernova.runtime.module.extension.mule.internal.dsl.processor.xml.ZernovaSdkExtensionDslNamespaceInfoProvider;
  provides com.zernova.runtime.extension.api.loader.ExtensionModelLoaderProvider
      with com.zernova.runtime.module.extension.mule.api.loader.ZernovaSdkExtensionModelLoaderProvider;

  exports com.zernova.runtime.module.extension.mule.api.processor to
      spring.beans;

  exports com.zernova.runtime.module.extension.mule.internal.config.factory to
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.module.extension.mule.internal.config.provider to
      spring.beans;

  opens com.zernova.runtime.module.extension.mule.internal.config.factory to
      net.bytebuddy;
  opens com.zernova.runtime.module.extension.mule.internal.execution to
      spring.core;
}
