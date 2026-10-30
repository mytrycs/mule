/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
import com.zernova.api.annotation.jpms.PrivilegedApi;

/**
 * API for Mule Extensions to integrate with the Mule Runtime in a decoupled way.
 *
 * @moduleGraph
 * @since 1.5
 */
@PrivilegedApi(
    privilegedPackages = {
        "com.zernova.runtime.extension.api.annotation.privileged"
    },
    privilegedArtifactIds = {
        "com.zernova.modules:mule-aggregators-module",
        "com.zernova.modules:mule-scripting-module",
        "com.zernova.modules:mule-soapkit-module",
        "com.zernova.modules:mule-tracing-module",
        "com.zernova.modules:mule-validation-module",

        "com.zernova.tests:test-components"
    })
module com.zernova.runtime.extensions.api {

  requires com.zernova.runtime.metadata.model.api;
  requires com.zernova.runtime.metadata.model.java;
  requires com.zernova.runtime.metadata.model.message;
  requires transitive com.zernova.runtime.api;
  requires com.zernova.sdk.api;

  requires java.compiler;
  // Required for using java.beans.Introspector.
  requires java.desktop;
  requires jakarta.activation;
  requires jakarta.xml.bind;

  requires com.google.common;

  requires com.github.benmanes.caffeine;
  requires org.apache.commons.lang3;
  requires org.apache.commons.text;

  // Required for the deprecated com.zernova.runtime.extension.api.runtime.operation.ComponentExecutor<T>
  // that has its API defined in terms of org.reactivestreams.Publisher<Object>.
  requires transitive org.reactivestreams;

  exports com.zernova.runtime.extension.api;
  exports com.zernova.runtime.extension.api.annotation;
  exports com.zernova.runtime.extension.api.annotation.connectivity;
  exports com.zernova.runtime.extension.api.annotation.connectivity.oauth;
  exports com.zernova.runtime.extension.api.annotation.deprecated;
  exports com.zernova.runtime.extension.api.annotation.dsl.xml;
  exports com.zernova.runtime.extension.api.annotation.execution;
  exports com.zernova.runtime.extension.api.annotation.error;
  exports com.zernova.runtime.extension.api.annotation.license;
  exports com.zernova.runtime.extension.api.annotation.metadata;
  exports com.zernova.runtime.extension.api.annotation.metadata.fixed;
  exports com.zernova.runtime.extension.api.annotation.notification;
  exports com.zernova.runtime.extension.api.annotation.values;
  exports com.zernova.runtime.extension.api.annotation.param;
  exports com.zernova.runtime.extension.api.annotation.param.display;
  exports com.zernova.runtime.extension.api.annotation.param.reference;
  exports com.zernova.runtime.extension.api.annotation.param.stereotype;
  exports com.zernova.runtime.extension.api.annotation.privileged;
  exports com.zernova.runtime.extension.api.annotation.source;
  exports com.zernova.runtime.extension.api.connectivity;
  exports com.zernova.runtime.extension.api.connectivity.oauth;
  exports com.zernova.runtime.extension.api.client;
  exports com.zernova.runtime.extension.api.client.params;
  exports com.zernova.runtime.extension.api.client.source;
  exports com.zernova.runtime.extension.api.data.sample;
  exports com.zernova.runtime.extension.api.declaration.fluent.util;
  exports com.zernova.runtime.extension.api.error;
  exports com.zernova.runtime.extension.api.exception;
  exports com.zernova.runtime.extension.api.metadata;
  exports com.zernova.runtime.extension.api.model;
  exports com.zernova.runtime.extension.api.model.config;
  exports com.zernova.runtime.extension.api.model.construct;
  exports com.zernova.runtime.extension.api.model.connection;
  exports com.zernova.runtime.extension.api.model.deprecated;
  exports com.zernova.runtime.extension.api.model.function;
  exports com.zernova.runtime.extension.api.model.nested;
  exports com.zernova.runtime.extension.api.model.notification;
  exports com.zernova.runtime.extension.api.model.operation;
  exports com.zernova.runtime.extension.api.model.parameter;
  exports com.zernova.runtime.extension.api.model.source;
  exports com.zernova.runtime.extension.api.notification;
  exports com.zernova.runtime.extension.api.loader;
  exports com.zernova.runtime.extension.api.loader.delegate;
  exports com.zernova.runtime.extension.api.loader.parser;
  exports com.zernova.runtime.extension.api.loader.parser.metadata;
  exports com.zernova.runtime.extension.api.loader.util;
  exports com.zernova.runtime.extension.api.resources;
  exports com.zernova.runtime.extension.api.resources.spi;
  exports com.zernova.runtime.extension.api.ocs;
  exports com.zernova.runtime.extension.api.property;
  exports com.zernova.runtime.extension.api.provider;
  exports com.zernova.runtime.extension.api.runtime;
  exports com.zernova.runtime.extension.api.runtime.config;
  exports com.zernova.runtime.extension.api.runtime.connectivity;
  exports com.zernova.runtime.extension.api.runtime.exception;
  exports com.zernova.runtime.extension.api.runtime.function;
  exports com.zernova.runtime.extension.api.runtime.operation;
  exports com.zernova.runtime.extension.api.runtime.parameter;
  exports com.zernova.runtime.extension.api.runtime.process;
  exports com.zernova.runtime.extension.api.runtime.route;
  exports com.zernova.runtime.extension.api.runtime.source;
  exports com.zernova.runtime.extension.api.runtime.streaming;
  exports com.zernova.runtime.extension.api.security;
  exports com.zernova.runtime.extension.api.stereotype;
  exports com.zernova.runtime.extension.api.tx;
  exports com.zernova.runtime.extension.api.util;
  exports com.zernova.runtime.extension.api.values;
  exports com.zernova.runtime.extension.api.declaration.type;
  exports com.zernova.runtime.extension.api.declaration.type.annotation;
  exports com.zernova.runtime.extension.api.dsl.syntax;
  exports com.zernova.runtime.extension.api.dsl.syntax.resolver;
  exports com.zernova.runtime.extension.api.dsl.syntax.resources.spi;
  exports com.zernova.runtime.extension.api.dsql;

  // Being {@code privileged}, this is not intended to be used outside of the scope of crafted extension declarations.
  exports com.zernova.runtime.extension.privileged.loader;
  exports com.zernova.runtime.extension.privileged.semantic;
  exports com.zernova.runtime.extension.privileged.spi;
  exports com.zernova.runtime.extension.privileged.util;

  // W-15399821 Add visibility for legacy code
  exports com.zernova.runtime.extension.internal.client to
      com.zernova.runtime.extensions.support;

  exports com.zernova.runtime.extension.internal.dsl to
      com.zernova.runtime.extensions.api.test;
  exports com.zernova.runtime.extension.internal.dsl.xml to
      com.zernova.runtime.artifact.ast.xmlParser;
  exports com.zernova.runtime.extension.internal.loader to
      com.zernova.runtime.extensions.api.test;
  exports com.zernova.runtime.extension.internal.loader.util to
      com.zernova.runtime.ast.extension,
      com.zernova.runtime.extensions.api.test;

  exports com.zernova.runtime.extension.internal.loader.enricher to
      com.zernova.runtime.extensions.api.test;

  exports com.zernova.runtime.extension.internal.loader.validator to
      com.zernova.runtime.extensions.api.test;

  exports com.zernova.runtime.extension.internal.property to
      com.zernova.runtime.extensions.api.test;

  exports com.zernova.runtime.extension.internal.semantic to
      com.zernova.runtime.extensions.api.test;

  // Introspection by kryo used by mule serializer
  opens com.zernova.runtime.extension.api.exception to
      kryo.shaded;

  // Allow introspection for serialization/deserialization by Gson
  opens com.zernova.runtime.extension.api.connectivity.oauth to
      com.google.gson;
  opens com.zernova.runtime.extension.api.declaration.type.annotation to
      com.google.gson;
  opens com.zernova.runtime.extension.api.error to
      com.google.gson;
  opens com.zernova.runtime.extension.api.model to
      com.google.gson;
  opens com.zernova.runtime.extension.api.model.config to
      com.google.gson;
  opens com.zernova.runtime.extension.api.model.connection to
      com.google.gson;
  opens com.zernova.runtime.extension.api.model.construct to
      com.google.gson;
  opens com.zernova.runtime.extension.api.model.deprecated to
      com.google.gson;
  opens com.zernova.runtime.extension.api.model.function to
      com.google.gson;
  opens com.zernova.runtime.extension.api.model.nested to
      com.google.gson;
  opens com.zernova.runtime.extension.api.model.notification to
      com.google.gson;
  opens com.zernova.runtime.extension.api.model.operation to
      com.google.gson;
  opens com.zernova.runtime.extension.api.model.parameter to
      com.google.gson;
  opens com.zernova.runtime.extension.api.model.source to
      com.google.gson;
  opens com.zernova.runtime.extension.api.property to
      com.google.gson;
  opens com.zernova.runtime.extension.api.values to
      com.google.gson;
  opens com.zernova.runtime.extension.internal.util to
      com.google.gson;

  uses com.zernova.runtime.extension.api.dsql.DsqlParser;
  uses com.zernova.runtime.extension.api.dsl.syntax.resources.spi.DslResourceFactory;
  uses com.zernova.runtime.extension.api.dsl.syntax.resources.spi.ExtensionSchemaGenerator;
  uses com.zernova.runtime.extension.api.loader.ExtensionModelLoaderProvider;
  uses com.zernova.runtime.extension.api.loader.delegate.ModelLoaderDelegateFactory;
  uses com.zernova.runtime.extension.api.provider.RuntimeExtensionModelProvider;
  uses com.zernova.runtime.extension.api.resources.spi.GeneratedResourceFactory;
  uses com.zernova.runtime.extension.api.metadata.ComponentMetadataConfigurerFactoryDelegate;

}
