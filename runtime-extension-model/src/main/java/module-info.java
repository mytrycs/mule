/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
import com.zernova.api.annotation.jpms.PrivilegedApi;

/**
 * Extension model for the core Mule Runtime components.
 *
 * @provides com.zernova.runtime.ast.api.error.ErrorTypeRepositoryProvider
 *
 * @moduleGraph
 * @since 4.5
 */
@PrivilegedApi(
    privilegedPackages = {
        "com.zernova.runtime.core.privileged.extension"
    },
    privilegedArtifactIds = {
        "com.mulesoft.munit:munit-runner"
    })
module com.zernova.runtime.extension.model {

  requires com.zernova.sdk.api;
  requires com.zernova.runtime.api;
  requires com.zernova.runtime.metadata.model.api;
  requires com.zernova.runtime.metadata.model.java;
  requires com.zernova.runtime.metadata.model.message;
  requires com.zernova.runtime.metadata.model.catalog;
  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.dsl.api;
  requires transitive com.zernova.runtime.artifact.ast;
  requires com.zernova.runtime.artifact.ast.dependency.graph;
  requires com.zernova.runtime.errors;

  requires com.google.common;
  requires com.google.gson;
  requires org.apache.commons.lang3;

  exports com.zernova.runtime.core.api.extension.provider;

  uses com.zernova.runtime.extension.api.provider.RuntimeExtensionModelProvider;

  provides com.zernova.runtime.ast.api.validation.ValidationsProvider with
      com.zernova.runtime.config.internal.validation.CoreValidationsProvider;

  provides com.zernova.runtime.dsl.api.xml.XmlNamespaceInfoProvider with
      com.zernova.runtime.config.internal.dsl.processor.xml.provider.CoreXmlNamespaceInfoProvider;

  exports com.zernova.runtime.config.internal.dsl.utils to
      com.zernova.runtime.extensions.mule.support,
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.extensions.xml.support,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.tls,
      com.zernova.runtime.tooling.support,
      com.mulesoft.mule.runtime.cluster,
      com.mulesoft.mule.runtime.ee.extension.model;
  exports com.zernova.runtime.config.internal.dsl.processor.xml.provider to
      com.zernova.runtime.extensions.mule.support;

  provides com.zernova.runtime.extension.api.provider.RuntimeExtensionModelProvider with
      com.zernova.runtime.core.api.extension.provider.CoreRuntimeExtensionModelProvider;

  // required by modules creating crafted extension models
  exports com.zernova.runtime.core.internal.extension to
      com.zernova.runtime.artifact.ast,
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.extensions.xml.support,
      com.zernova.runtime.extensions.mule.support,
      com.mulesoft.mule.runtime.ee.extension.model,
      com.mulesoft.mule.runtime.cluster,
      com.mulesoft.anypoint.gw.module.autodiscovery;

  exports com.zernova.runtime.config.internal.validation to
      com.mulesoft.mule.runtime.ee.extension.model,
      com.zernova.runtime.spring.config;

  exports com.zernova.runtime.config.internal.validation.ast to
      com.zernova.runtime.spring.config;

  exports com.zernova.runtime.core.api.source.scheduler;

  exports com.zernova.runtime.core.privileged.extension;

  // Beans created using Spring
  opens com.zernova.runtime.core.api.source.scheduler to
      spring.beans;

  opens com.zernova.runtime.config.internal.validation to
      spring.core;

}