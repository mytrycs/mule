/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
import com.zernova.api.annotation.jpms.PrivilegedApi;

/**
 * Default implementation of the Mule Extension API.
 *
 * @moduleGraph
 * @since 4.6
 */
@PrivilegedApi(
  privilegedPackages = {
        "com.zernova.runtime.module.extension.api.runtime.privileged"
  },
  privilegedArtifactIds = {
      "com.mulesoft.munit:munit-tools",
      "com.zernova.modules:mule-aggregators-module",
      "com.zernova.modules:mule-scripting-module",
      "com.zernova.modules:mule-soapkit-module",
      "com.zernova.modules:mule-tracing-module",
      "com.zernova.modules:mule-validation-module"
  })
module com.zernova.runtime.extensions.support {

  requires com.zernova.oauth.client.api;
  requires com.zernova.runtime.artifact;
  requires com.zernova.runtime.artifact.activation;
  requires com.zernova.runtime.artifact.ast;
  requires transitive com.zernova.runtime.core;
  requires com.zernova.runtime.dsl.api;
  requires com.zernova.runtime.errors;
  requires com.zernova.runtime.extension.model;
  requires transitive com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.extensions.api.persistence;
  requires com.zernova.runtime.featureManagement;
  requires com.zernova.runtime.http.api;
  requires com.zernova.runtime.manifest;
  requires com.zernova.runtime.metadata.model.api;
  requires com.zernova.runtime.metadata.model.catalog;
  requires com.zernova.runtime.metadata.model.java;
  requires com.zernova.runtime.metadata.model.json;
  requires com.zernova.runtime.metadata.model.message;
  requires com.zernova.runtime.metadata.model.xml;
  requires com.zernova.runtime.metadata.support;
  requires com.zernova.runtime.oauth.api;
  requires com.zernova.runtime.policy.api;
  requires com.zernova.runtime.profiling.api;
  requires com.zernova.runtime.tracer.api;
  requires com.zernova.runtime.tracer.customization.api;
  requires com.zernova.runtime.tracer.internal.impl;
  requires com.zernova.sdk.api;
  requires com.zernova.sdk.compatibility.api;

  // Usage of TypeElement, VariableElement and other
  requires java.compiler;
  // XML documentation classes
  requires jakarta.xml.bind;

  requires com.github.benmanes.caffeine;
  requires com.google.common;
  requires org.apache.commons.io;
  requires org.apache.commons.lang3;
  // Date Times and formats
  requires org.joda.time;
  requires reactor.core;
  requires org.reflections;
  requires spring.core;
  // Usage of java.beans
  requires java.desktop;
  requires net.bytebuddy;
  requires org.jgrapht.core;

  exports com.zernova.runtime.module.extension.api.manager;
  exports com.zernova.runtime.module.extension.api.loader;
  exports com.zernova.runtime.module.extension.api.loader.java.property;
  exports com.zernova.runtime.module.extension.api.loader.java.type;
  exports com.zernova.runtime.module.extension.api.metadata;
  exports com.zernova.runtime.module.extension.api.runtime.compatibility;
  exports com.zernova.runtime.module.extension.api.runtime.config;
  exports com.zernova.runtime.module.extension.api.runtime.connectivity.oauth;
  exports com.zernova.runtime.module.extension.api.runtime.executor;
  exports com.zernova.runtime.module.extension.api.runtime.resolver;
  exports com.zernova.runtime.module.extension.api.tooling;
  exports com.zernova.runtime.module.extension.api.tooling.metadata;
  exports com.zernova.runtime.module.extension.api.tooling.sampledata;
  exports com.zernova.runtime.module.extension.api.tooling.valueprovider;
  exports com.zernova.runtime.module.extension.api.util;
  // Used by the extensions-maven-plugin
  exports com.zernova.runtime.module.extension.api.resources.documentation;

  exports com.zernova.runtime.module.extension.api.runtime.privileged to
      com.zernova.runtime.extensions.spring.support;

  // for ByteBuddy dynamically generated classes
  exports com.zernova.runtime.module.extension.privileged.component;

  exports com.zernova.runtime.module.extension.internal to
      com.zernova.runtime.extensions.spring.support,
      com.zernova.test.runner;
  exports com.zernova.runtime.module.extension.internal.config to
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.extensions.xml.support;
  exports com.zernova.runtime.module.extension.internal.data.sample to
      com.zernova.runtime.extensions.tooling.support;
  exports com.zernova.runtime.module.extension.internal.loader to
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.extensions.mule.support,
      com.zernova.runtime.extensions.xml.support;
  exports com.zernova.runtime.module.extension.internal.loader.java to
      com.zernova.runtime.core,
      com.zernova.runtime.extensions.spring.support,
      com.zernova.test.runner;
  exports com.zernova.runtime.module.extension.internal.loader.java.info to
      com.zernova.runtime.extensions.spring.support;
  exports com.zernova.runtime.module.extension.internal.loader.java.property to
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.extensions.mule.support,
      com.zernova.runtime.extensions.xml.support;
  exports com.zernova.runtime.module.extension.internal.loader.java.type.property to
      com.zernova.runtime.extensions.spring.support;
  exports com.zernova.runtime.module.extension.internal.loader.parser.java to
      com.zernova.runtime.extensions.spring.support;
  exports com.zernova.runtime.module.extension.internal.loader.parser.java.stereotypes to
      com.zernova.runtime.extensions.spring.support;
  exports com.zernova.runtime.module.extension.internal.loader.utils to
      com.zernova.runtime.extensions.mule.support,
      com.zernova.runtime.extensions.spring.support;
  exports com.zernova.runtime.module.extension.internal.manager to
      com.zernova.runtime.spring.config,
      com.zernova.test.runner,
      com.zernova.runtime.deployment.model.impl;
  exports com.zernova.runtime.module.extension.internal.resources.manifest to
      com.zernova.runtime.extensions.spring.support;
  exports com.zernova.runtime.module.extension.internal.runtime to
      com.zernova.runtime.extensions.spring.support;
  exports com.zernova.runtime.module.extension.internal.runtime.client to
      com.zernova.runtime.core;
  exports com.zernova.runtime.module.extension.internal.runtime.connectivity to
      com.zernova.runtime.core,
      com.zernova.runtime.extensions.spring.support;
  exports com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.authcode to
      com.zernova.runtime.core,
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.spring.config;
  exports com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.clientcredentials to
      com.zernova.runtime.core,
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.spring.config;
  exports com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ocs to
      com.zernova.runtime.core,
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.spring.config;
  exports com.zernova.runtime.module.extension.internal.runtime.execution.deprecated to
      com.zernova.runtime.core;
  exports com.zernova.runtime.module.extension.internal.runtime.execution.executor to
      com.zernova.runtime.core,
      com.zernova.runtime.extensions.xml.support;
  exports com.zernova.runtime.module.extension.internal.runtime.exception to
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.extensions.xml.support;
  exports com.zernova.runtime.module.extension.internal.runtime.objectbuilder to
      com.zernova.runtime.extensions.spring.support;
  exports com.zernova.runtime.module.extension.internal.runtime.operation to
      com.zernova.runtime.extensions.spring.support;
  exports com.zernova.runtime.module.extension.internal.runtime.resolver to
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.extensions.xml.support;
  exports com.zernova.runtime.module.extension.internal.runtime.resolver.resolver to
      com.zernova.runtime.extensions.spring.support;
  exports com.zernova.runtime.module.extension.internal.runtime.source to
      com.zernova.runtime.core,
      com.zernova.runtime.extensions.spring.support;
  exports com.zernova.runtime.module.extension.internal.runtime.transaction to
      com.zernova.runtime.core;
  exports com.zernova.runtime.module.extension.internal.store to
      com.zernova.runtime.spring.config;
  exports com.zernova.runtime.module.extension.internal.util to
      com.zernova.runtime.core,
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.extensions.xml.support,
      com.zernova.runtime.tooling.support,
      com.zernova.runtime.spring.config;
  exports com.zernova.runtime.module.extension.internal.loader.java.enricher to
      com.zernova.runtime.extensions.mule.support;
  exports com.zernova.runtime.module.extension.internal.loader.java.type.runtime to
      com.zernova.runtime.extensions.spring.support;
  exports com.zernova.runtime.module.extension.internal.type.catalog to
      com.zernova.runtime.extensions.mule.support,
      com.zernova.runtime.spring.config;
  exports com.zernova.runtime.module.extension.internal.loader.parser.java.utils to
      com.zernova.runtime.extensions.mule.support;
  exports com.zernova.runtime.module.extension.internal.loader.parser.java.metadata to
      com.zernova.runtime.extensions.mule.support,
      com.zernova.runtime.extensions.spring.support;

  opens com.zernova.runtime.module.extension.internal.resources.documentation to
      jakarta.xml.bind;

  provides com.zernova.runtime.api.connectivity.ConnectivityTestingStrategy with
      com.zernova.runtime.module.extension.api.tooling.ExtensionConnectivityTestingStrategy;
  provides com.zernova.runtime.extension.api.loader.ExtensionModelLoaderProvider with
      com.zernova.runtime.module.extension.api.loader.DefaultExtensionModelLoaderProvider;
  provides com.zernova.runtime.extension.api.resources.spi.GeneratedResourceFactory with
      com.zernova.runtime.module.extension.internal.resources.documentation.ExtensionDocumentationResourceGenerator;
  provides com.zernova.runtime.core.privileged.transaction.TransactionFactory with
      com.zernova.runtime.module.extension.internal.runtime.transaction.ExtensionTransactionFactory;
  provides com.zernova.runtime.extension.api.metadata.ComponentMetadataConfigurerFactoryDelegate with
      com.zernova.runtime.module.extension.internal.metadata.DefaultComponentMetadataConfigurerFactoryDelegate;
  provides com.zernova.runtime.extension.api.loader.delegate.ModelLoaderDelegateFactory with
      com.zernova.runtime.module.extension.internal.loader.delegate.DefaultExtensionModelLoaderDelegateFactory;

}
