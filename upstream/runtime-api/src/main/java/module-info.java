/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
import com.zernova.api.annotation.jpms.PrivilegedApi;

/**
 * API for Mule Runtime.
 *
 * @moduleGraph
 * @since 1.5
 */
@PrivilegedApi(
    privilegedPackages = {
        "com.zernova.runtime.privileged.event"
    },
    privilegedArtifactIds = {})
module com.zernova.runtime.api {

  requires transitive com.zernova.runtime.api.annotations;
  requires com.zernova.runtime.metadata.model.api;
  requires com.zernova.runtime.metadata.model.message;
  requires com.zernova.runtime.artifact.declaration;
  requires com.zernova.runtime.artifact.declaration.persistence;

  requires jakarta.activation;
  requires java.naming;
  requires java.sql;
  // for QName, used for component annotations
  requires transitive java.xml;
  requires transitive jakarta.inject;
  // Old Java EE dependencies (with javax package prefix), kept for backwards compatibility
  requires transitive javax.inject;

  // SLF4J is the API provided by the Mule Runtime
  // for artifacts to use the underlying logging functionality
  requires transitive org.slf4j;

  requires com.github.benmanes.caffeine;
  requires com.google.common;
  requires com.google.gson;
  requires org.apache.commons.io;
  requires org.apache.commons.lang3;
  requires semver4j;

  exports com.zernova.runtime.api.alert;
  exports com.zernova.runtime.api.app.declaration.serialization;
  exports com.zernova.runtime.api.artifact;
  exports com.zernova.runtime.api.bulk;
  exports com.zernova.runtime.api.util.classloader;
  exports com.zernova.runtime.api.cluster;
  exports com.zernova.runtime.api.component;
  exports com.zernova.runtime.api.component.execution;
  exports com.zernova.runtime.api.component.location;
  exports com.zernova.runtime.api.config;
  exports com.zernova.runtime.api.config.custom;
  exports com.zernova.runtime.api.connection;
  exports com.zernova.runtime.api.connection.serialization;
  exports com.zernova.runtime.api.connectivity;
  exports com.zernova.runtime.api.deployment.management;
  exports com.zernova.runtime.api.deployment.meta;
  exports com.zernova.runtime.api.deployment.persistence;
  exports com.zernova.runtime.api.dsl;
  exports com.zernova.runtime.api.el;
  exports com.zernova.runtime.api.el.validation;
  exports com.zernova.runtime.api.el.validation.constraints;
  exports com.zernova.runtime.api.event;
  exports com.zernova.runtime.api.exception;
  exports com.zernova.runtime.api.functional;
  exports com.zernova.runtime.api.healthcheck;
  exports com.zernova.runtime.api.i18n;
  exports com.zernova.runtime.api.ioc;
  exports com.zernova.runtime.api.interception;
  exports com.zernova.runtime.api.legacy.exception;
  exports com.zernova.runtime.api.lifecycle;
  exports com.zernova.runtime.api.lock;
  exports com.zernova.runtime.api.message;
  exports com.zernova.runtime.api.message.error.matcher;
  exports com.zernova.runtime.api.meta;
  exports com.zernova.runtime.api.meta.type;
  exports com.zernova.runtime.api.meta.model;
  exports com.zernova.runtime.api.meta.model.config;
  exports com.zernova.runtime.api.meta.model.data.sample;
  exports com.zernova.runtime.api.meta.model.declaration.fluent;
  exports com.zernova.runtime.api.meta.model.declaration.fluent.util;
  exports com.zernova.runtime.api.meta.model.deprecated;
  exports com.zernova.runtime.api.meta.model.connection;
  exports com.zernova.runtime.api.meta.model.construct;
  exports com.zernova.runtime.api.meta.model.display;
  exports com.zernova.runtime.api.meta.model.error;
  exports com.zernova.runtime.api.meta.model.function;
  exports com.zernova.runtime.api.meta.model.nested;
  exports com.zernova.runtime.api.meta.model.notification;
  exports com.zernova.runtime.api.meta.model.operation;
  exports com.zernova.runtime.api.meta.model.parameter;
  exports com.zernova.runtime.api.meta.model.source;
  exports com.zernova.runtime.api.meta.model.stereotype;
  exports com.zernova.runtime.api.meta.model.util;
  exports com.zernova.runtime.api.meta.model.version;
  exports com.zernova.runtime.api.memory.management;
  exports com.zernova.runtime.api.memory.provider;
  exports com.zernova.runtime.api.memory.provider.type;
  exports com.zernova.runtime.api.metadata;
  exports com.zernova.runtime.api.metadata.descriptor;
  exports com.zernova.runtime.api.metadata.resolving;
  exports com.zernova.runtime.api.notification;
  exports com.zernova.runtime.api.parameterization;
  exports com.zernova.runtime.api.retry.policy;
  exports com.zernova.runtime.api.sampledata;
  exports com.zernova.runtime.api.scheduler;
  exports com.zernova.runtime.api.security;
  exports com.zernova.runtime.api.serialization;
  exports com.zernova.runtime.api.service;
  exports com.zernova.runtime.api.source;
  exports com.zernova.runtime.api.store;
  exports com.zernova.runtime.api.streaming;
  exports com.zernova.runtime.api.streaming.bytes;
  exports com.zernova.runtime.api.streaming.object;
  exports com.zernova.runtime.api.streaming.exception;
  exports com.zernova.runtime.api.time;
  exports com.zernova.runtime.api.tls;
  exports com.zernova.runtime.api.transformation;
  exports com.zernova.runtime.api.tx;
  exports com.zernova.runtime.api.util;
  exports com.zernova.runtime.api.util.collection;
  exports com.zernova.runtime.api.util.concurrent;
  exports com.zernova.runtime.api.util.xmlsecurity;
  exports com.zernova.runtime.api.value;

  uses com.zernova.runtime.api.config.custom.ServiceConfigurator;
  uses com.zernova.runtime.api.connectivity.ConnectivityTestingStrategy;
  uses com.zernova.runtime.api.el.AbstractBindingContextBuilderFactory;
  uses com.zernova.runtime.api.message.AbstractZernovaMessageBuilderFactory;
  uses com.zernova.runtime.api.metadata.AbstractDataTypeBuilderFactory;
  uses com.zernova.runtime.api.tls.AbstractTlsContextFactoryBuilderFactory;

  // for DataWeave
  exports com.zernova.runtime.privileged.event;
  exports com.zernova.runtime.privileged.exception;

  // Internals exposed to test module
  exports com.zernova.runtime.internal.exception to
      com.zernova.runtime.api.test;
  exports com.zernova.runtime.internal.util to
      com.zernova.runtime.api.test;
  exports com.zernova.runtime.internal.util.collection to
      com.zernova.runtime.api.test;
  exports com.zernova.runtime.internal.util.xmlsecurity to
      com.zernova.runtime.api.test;
  exports com.zernova.runtime.internal.meta.model to
      com.zernova.runtime.artifact.ast;

  // Allow extensions-support to create objects from these packages dynamically
  opens com.zernova.runtime.api.connection to
      com.zernova.runtime.extensions.support,
      kryo.shaded;
  opens com.zernova.runtime.api.exception to
      com.zernova.runtime.extensions.support,
      // Introspection by kryo used by mule serializer
      kryo.shaded;
  opens com.zernova.runtime.privileged.exception to
      // Introspection by kryo used by mule serializer
      kryo.shaded;
  opens com.zernova.runtime.api.lifecycle to
      // Introspection by kryo used by mule serializer
      kryo.shaded;
  opens com.zernova.runtime.api.message to
      // Introspection by kryo used by mule serializer
      kryo.shaded;
  opens com.zernova.runtime.api.util to
      com.zernova.runtime.extensions.support,
      // Introspection by kryo used by mule serializer
      kryo.shaded;

  // Allow introspection for serialization/deserialization by Gson and Kryo
  opens com.zernova.runtime.api.component to
      com.zernova.runtime.extensions.support,
      com.google.gson,
      kryo.shaded;
  opens com.zernova.runtime.api.i18n to
      kryo.shaded;
  opens com.zernova.runtime.api.deployment.meta to
      com.google.gson;
  opens com.zernova.runtime.api.meta.model to
      com.google.gson;
  opens com.zernova.runtime.api.meta.model.data.sample to
      com.google.gson;
  opens com.zernova.runtime.api.meta.model.display to
      com.google.gson;
  opens com.zernova.runtime.api.meta.model.error to
      com.google.gson;
  opens com.zernova.runtime.api.meta.model.parameter to
      com.google.gson;
  opens com.zernova.runtime.api.meta.model.stereotype to
      com.google.gson;
  opens com.zernova.runtime.api.metadata to
      com.zernova.runtime.extensions.support,
      com.google.gson,
      com.zernova.runtime.api.test,
      // Introspection by kryo used by mule serializer
      kryo.shaded;
  opens com.zernova.runtime.api.metadata.resolving to
      com.google.gson;
  opens com.zernova.runtime.api.value to
      com.google.gson;

}
