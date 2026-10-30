/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
import com.zernova.api.annotation.jpms.PrivilegedApi;
import com.zernova.runtime.core.internal.artifact.ArtifactClassLoaderFinder;

/**
 * Mule server and core classes.
 *
 * @moduleGraph
 * @since 4.5
 */
@PrivilegedApi(
    privilegedPackages = {
        "com.zernova.runtime.core.internal.message",
        "com.zernova.runtime.core.privileged",
        "com.zernova.runtime.core.privileged.el",
        "com.zernova.runtime.core.privileged.event",
        "com.zernova.runtime.core.privileged.exception",
        "com.zernova.runtime.core.privileged.execution",
        "com.zernova.runtime.core.privileged.interception",
        "com.zernova.runtime.core.privileged.message",
        "com.zernova.runtime.core.privileged.metadata",
        "com.zernova.runtime.core.privileged.processor",
        "com.zernova.runtime.core.privileged.processor.chain",
        "com.zernova.runtime.core.privileged.processor.simple",
        "com.zernova.runtime.core.privileged.processor.objectfactory",
        "com.zernova.runtime.core.privileged.registry",
        "com.zernova.runtime.core.privileged.routing",
        "com.zernova.runtime.core.privileged.profiling",
        "com.zernova.runtime.core.privileged.profiling.tracing",
        "com.zernova.runtime.core.privileged.transaction"
    },
    privilegedArtifactIds = {
        "com.mulesoft.munit:munit-runner",
        "com.mulesoft.munit:munit-tools",
        "com.mulesoft.munit:mtf-tools",
        "com.zernova.modules:mule-aggregators-module",
        "com.zernova.modules:mule-apikit-module",
        "com.zernova.modules:mule-soapkit-module",
        "com.zernova.modules:mule-tracing-module",
        "com.zernova.modules:mule-validation-module",
        "com.zernova.modules:mule-scripting-module",
        "com.zernova.tests.plugin:mule-tests-component-plugin",
        "com.zernova.tests:test-processor-chains",
        "com.zernova.tests:test-components"
    })
module com.zernova.runtime.core {

  requires com.zernova.runtime.metadata.model.api;
  requires com.zernova.runtime.metadata.model.java;
  requires com.zernova.runtime.metadata.model.message;
  requires transitive com.zernova.runtime.api;
  requires com.zernova.sdk.api;
  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.policy.api;
  requires com.zernova.runtime.http.policy.api;
  requires com.zernova.runtime.profiling.api;
  requires com.zernova.runtime.dsl.api;
  requires com.zernova.runtime.artifact.ast;
  // module from the boot layer, will not be available in tests
  requires static com.zernova.runtime.logging;
  requires com.zernova.runtime.manifest;


  requires com.zernova.runtime.metrics.api;
  requires com.zernova.runtime.tracer.api;
  requires com.zernova.runtime.tracer.customization.api;
  requires com.zernova.runtime.tracer.exporter.api;
  requires com.zernova.runtime.tracer.exporter.configuration.api;

  requires com.zernova.runtime.featureManagement;
  requires com.zernova.runtime.errors;

  // reactor and friends
  requires org.reactivestreams;
  requires net.jodah.failsafe;
  requires reactor.core;
  requires reactor.extra;

  // utilities
  requires com.github.benmanes.caffeine;
  requires com.google.common;
  requires com.google.gson;
  requires org.apache.commons.collections4;
  requires org.apache.commons.io;
  requires org.apache.commons.lang3;
  requires org.apache.commons.pool2;
  requires org.jgrapht.core;
  requires net.bytebuddy;
  requires org.reflections;
  requires uuid;
  requires org.vibur.objectpool;
  requires semver4j;

  requires jakarta.activation;
  requires jakarta.annotation;
  requires transitive jakarta.jms.api;
  requires transitive jakarta.messaging;
  requires java.management;
  requires jakarta.transaction;

  exports com.zernova.runtime.core.api;
  exports com.zernova.runtime.core.api.alert;
  exports com.zernova.runtime.core.api.artifact;
  exports com.zernova.runtime.core.api.component;
  exports com.zernova.runtime.core.api.connection.util;
  exports com.zernova.runtime.core.api.config;
  exports com.zernova.runtime.core.api.config.bootstrap;
  exports com.zernova.runtime.core.api.config.builders;
  exports com.zernova.runtime.core.api.config.i18n;
  exports com.zernova.runtime.core.api.connector;
  exports com.zernova.runtime.core.api.construct;
  exports com.zernova.runtime.core.api.context;
  exports com.zernova.runtime.core.api.context.notification;
  exports com.zernova.runtime.core.api.context.thread.notification;
  exports com.zernova.runtime.core.api.data.sample;
  exports com.zernova.runtime.core.api.el;
  exports com.zernova.runtime.core.api.event;
  exports com.zernova.runtime.core.api.exception;
  exports com.zernova.runtime.core.api.execution;
  exports com.zernova.runtime.core.api.expression;
  exports com.zernova.runtime.core.api.extension;
  exports com.zernova.runtime.core.api.functional;
  exports com.zernova.runtime.core.api.lifecycle;
  exports com.zernova.runtime.core.api.management.stats;
  exports com.zernova.runtime.core.api.message;
  exports com.zernova.runtime.core.api.metadata;
  exports com.zernova.runtime.core.api.object;
  exports com.zernova.runtime.core.api.policy;
  exports com.zernova.runtime.core.api.processor;
  // Apart from Mule Runtime, this is used in integration tests and MUnit
  exports com.zernova.runtime.core.api.processor.strategy;
  exports com.zernova.runtime.core.api.registry;
  exports com.zernova.runtime.core.api.retry;
  exports com.zernova.runtime.core.api.retry.async;
  exports com.zernova.runtime.core.api.retry.policy;
  exports com.zernova.runtime.core.api.rx;
  exports com.zernova.runtime.core.api.security;
  exports com.zernova.runtime.core.api.source;
  exports com.zernova.runtime.core.api.streaming;
  exports com.zernova.runtime.core.api.streaming.bytes;
  exports com.zernova.runtime.core.api.streaming.bytes.factory;
  exports com.zernova.runtime.core.api.streaming.iterator;
  exports com.zernova.runtime.core.api.streaming.object;
  exports com.zernova.runtime.core.api.transaction;
  exports com.zernova.runtime.core.api.transaction.xa;
  exports com.zernova.runtime.core.api.transformer;
  exports com.zernova.runtime.core.api.util;
  exports com.zernova.runtime.core.api.util.concurrent;
  exports com.zernova.runtime.core.api.util.compression;
  exports com.zernova.runtime.core.api.util.func;
  exports com.zernova.runtime.core.api.util.proxy;
  exports com.zernova.runtime.core.api.util.queue;
  exports com.zernova.runtime.core.api.util.xmlsecurity;

  uses com.zernova.runtime.core.internal.transaction.TypedTransactionFactory;

  provides com.zernova.runtime.api.el.AbstractBindingContextBuilderFactory with
      com.zernova.runtime.core.api.el.DefaultBindingContextBuilderFactory;

  provides com.zernova.runtime.api.el.AbstractExpressionModuleBuilderFactory with
      com.zernova.runtime.core.internal.el.DefaultExpressionModuleBuilderFactory;

  provides com.zernova.runtime.api.message.AbstractZernovaMessageBuilderFactory with
      com.zernova.runtime.core.internal.message.DefaultMessageBuilderFactory;

  provides com.zernova.runtime.api.metadata.AbstractDataTypeBuilderFactory with
      com.zernova.runtime.core.api.metadata.DefaultDataTypeBuilderFactory;

  provides com.zernova.runtime.core.internal.transaction.TypedTransactionFactory with
      com.zernova.runtime.core.internal.transaction.DelegateTransactionFactory;

  // for MUnit, MTF
  exports com.zernova.runtime.core.privileged;
  // for MUnit and muleFwk
  exports com.zernova.runtime.core.privileged.el;
  // for MuleFwk, MUnit, MTF, ApiGateway, Tracing and Validation
  exports com.zernova.runtime.core.privileged.event;
  // for Kryo
  exports com.zernova.runtime.core.privileged.event.context to
      com.mulesoft.mule.runtime.kryo;
  // for MuleFwk, MUnit, ApiKit, DataWeave and Validation
  exports com.zernova.runtime.core.privileged.exception;
  // for test-components
  exports com.zernova.runtime.core.privileged.execution;
  // for MUnit
  exports com.zernova.runtime.core.privileged.interception;
  // for MUnit
  exports com.zernova.runtime.core.privileged.message;
  // for DataWeave
  exports com.zernova.runtime.core.privileged.metadata;
  // for MUnit, MTF, ApiKit and Validation
  exports com.zernova.runtime.core.privileged.processor;
  // for MUnit and Validation
  exports com.zernova.runtime.core.privileged.processor.chain;
  // for MTF
  exports com.zernova.runtime.core.privileged.processor.objectfactory;
  // for MTF
  exports com.zernova.runtime.core.privileged.processor.simple;
  // for test-components
  exports com.zernova.runtime.core.privileged.profiling;
  exports com.zernova.runtime.core.privileged.profiling.tracing to
      com.zernova.runtime.core.components;
  // for Aggregators
  exports com.zernova.runtime.core.privileged.registry;
  // for DataWeave
  exports com.zernova.runtime.core.privileged.routing;
  // for test-components
  exports com.zernova.runtime.core.privileged.transaction;

  exports com.zernova.runtime.core.internal.alert to
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.core.internal.artifact to
      com.zernova.runtime.artifact.activation;
  exports com.zernova.runtime.core.internal.cluster to
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.core.internal.config to
      com.zernova.runtime.deployment,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.deployment.model.impl,
      com.mulesoft.mule.runtime.cluster,
      spring.beans;
  exports com.zernova.runtime.core.internal.config.bootstrap to
      com.zernova.runtime.deployment.model.impl,
      com.zernova.runtime.spring.config;
  exports com.zernova.runtime.core.internal.config.builders to
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config,
      com.zernova.test.unit;
  exports com.zernova.runtime.core.internal.connection to
      com.mulesoft.mule.runtime.bti,
      com.zernova.runtime.deployment.model.impl,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.extensions.xml.support,
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.core.internal.connection.adapter to
      com.zernova.runtime.extensions.support;
  exports com.zernova.runtime.core.internal.connector to
      com.mulesoft.mule.runtime.cluster;
  exports com.zernova.runtime.core.internal.construct to
      com.zernova.runtime.core.components,
      com.zernova.runtime.deployment,
      com.zernova.runtime.deployment.model.impl,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config;
  exports com.zernova.runtime.core.internal.context to
      com.zernova.runtime.core.components,
      com.zernova.runtime.artifact,
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.deployment,
      com.zernova.runtime.deployment.model.impl,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.launcher,
      com.mulesoft.mule.runtime.bti,
      com.mulesoft.mule.runtime.kryo,
      com.mulesoft.mule.runtime.batch,
      com.mulesoft.mule.runtime.cluster,
      com.zernova.test.unit,
      com.zernova.test.runner;
  exports com.zernova.runtime.core.internal.context.notification to
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.extensions.xml.support,
      com.zernova.runtime.spring.config,
      com.mulesoft.mule.runtime.bti,
      com.mulesoft.mule.runtime.kryo,
      spring.beans;
  // Needed for byte-buddy proxies (generated in the unnamed-module) for visibility
  exports com.zernova.runtime.core.internal.component to
      com.zernova.runtime.core.components,
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config,
      com.mulesoft.mule.runtime.batch;
  exports com.zernova.runtime.core.internal.el to
      com.zernova.runtime.core.components,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.extensions.xml.support,
      com.zernova.runtime.spring.config,
      com.mulesoft.mule.runtime.batch,
      com.mulesoft.mule.runtime.core.ee,
      spring.beans;
  exports com.zernova.runtime.core.internal.el.dataweave to
      com.zernova.runtime.core.components,
      com.zernova.runtime.spring.config;
  exports com.zernova.runtime.core.internal.el.function to
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.core.internal.event to
      com.zernova.runtime.core.components,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.extensions.xml.support,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.tooling.support,
      com.mulesoft.mule.runtime.batch,
      com.mulesoft.mule.runtime.core.ee,
      com.mulesoft.mule.runtime.kryo,
      spring.beans;
  exports com.zernova.runtime.core.internal.exception to
      com.zernova.runtime.core.components,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.extensions.xml.support,
      com.zernova.runtime.spring.config,
      com.mulesoft.mule.runtime.batch,
      spring.beans;
  exports com.zernova.runtime.core.internal.execution to
      com.zernova.runtime.core.components,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.properties.config,
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.core.internal.interception to
      com.zernova.runtime.core.components,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config;
  exports com.zernova.runtime.core.internal.lifecycle to
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config,
      com.zernova.test.runner;
  exports com.zernova.runtime.core.internal.lifecycle.phases to
      com.zernova.runtime.deployment.model.impl,
      com.zernova.runtime.spring.config;
  exports com.zernova.runtime.core.internal.lock to
      com.zernova.runtime.deployment.model.impl,
      com.zernova.runtime.launcher,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.tooling.support,
      com.mulesoft.mule.runtime.batch,
      com.mulesoft.mule.runtime.cluster,
      spring.beans;
  exports com.zernova.runtime.core.internal.management.stats to
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config,
      spring.beans;
  // Required because this is used in test components that end up in the unnamed module
  // and for MUnit and ApiKit
  exports com.zernova.runtime.core.internal.message;
  exports com.zernova.runtime.core.internal.metadata to
      com.mulesoft.mule.runtime.kryo;
  exports com.zernova.runtime.core.internal.policy to
      com.zernova.runtime.core.components,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.spring.config,
      com.mulesoft.mule.runtime.http.policy,
      spring.beans;
  exports com.zernova.runtime.core.internal.processor.chain to
      com.zernova.runtime.spring.config;
  exports com.zernova.runtime.core.internal.processor.interceptor to
      com.zernova.runtime.spring.config,
      spring.beans,
      kryo.shaded;
  exports com.zernova.runtime.core.internal.processor.strategy to
      com.zernova.runtime.core.components,
      com.zernova.runtime.extensions.support;
  exports com.zernova.runtime.core.internal.processor.strategy.util to
      com.zernova.runtime.extensions.support;
  exports com.zernova.runtime.core.internal.profiling to
      com.zernova.runtime.core.components,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.deployment.model.impl,
      com.zernova.runtime.tracer.customization.impl,
      com.zernova.service.scheduler,
      com.mulesoft.mule.runtime.batch,
      spring.beans;
  exports com.zernova.runtime.core.internal.profiling.context to
      com.zernova.runtime.extensions.support;
  exports com.zernova.runtime.core.internal.profiling.tracing.event.span.condition to
      com.zernova.runtime.tracer.internal.impl;
  exports com.zernova.runtime.core.internal.registry to
      com.zernova.runtime.core.components,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.extensions.mule.support,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.deployment,
      com.zernova.runtime.deployment.model,
      com.zernova.runtime.deployment.model.impl,
      com.zernova.runtime.launcher,
      com.mulesoft.mule.runtime.bti,
      spring.beans,
      com.zernova.test.unit,
      com.zernova.test.runner;
  exports com.zernova.runtime.core.internal.retry to
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.spring.config;
  exports com.zernova.runtime.core.internal.routing.outbound to
      com.zernova.runtime.core.components,
      com.mulesoft.mule.runtime.batch;
  exports com.zernova.runtime.core.internal.routing.result to
      com.zernova.runtime.core.components,
      com.zernova.runtime.spring.config;
  exports com.zernova.runtime.core.internal.routing.split to
      com.zernova.runtime.core.components,
      com.mulesoft.mule.runtime.batch,
      kryo.shaded;
  exports com.zernova.runtime.core.internal.rx to
      com.zernova.runtime.core.components,
      com.zernova.runtime.extensions.support;
  exports com.zernova.runtime.core.internal.security to
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config,
      com.mulesoft.mule.runtime.kryo,
      spring.beans;
  exports com.zernova.runtime.core.internal.serialization to
      com.zernova.runtime.artifact,
      com.mulesoft.mule.runtime.core.ee,
      com.mulesoft.mule.runtime.kryo,
      com.zernova.test.unit,
      spring.beans;
  exports com.zernova.runtime.core.internal.store to
      com.zernova.runtime.spring.config,
      com.zernova.runtime.tooling.support,
      com.mulesoft.mule.runtime.cluster,
      com.mulesoft.mule.runtime.kryo,
      spring.beans;
  exports com.zernova.runtime.core.internal.streaming to
      com.zernova.runtime.core.components,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config,
      com.mulesoft.mule.runtime.core.ee,
      spring.beans;
  exports com.zernova.runtime.core.internal.streaming.bytes to
      com.zernova.runtime.extensions.support,
      com.mulesoft.mule.runtime.core.ee,
      com.mulesoft.mule.runtime.kryo;
  exports com.zernova.runtime.core.internal.streaming.bytes.factory to
      com.zernova.runtime.extensions.support;
  exports com.zernova.runtime.core.internal.streaming.object to
      com.mulesoft.mule.runtime.core.ee,
      com.mulesoft.mule.runtime.kryo;
  exports com.zernova.runtime.core.internal.streaming.object.factory to
      com.zernova.runtime.extensions.support,
      com.mulesoft.mule.runtime.core.ee;
  exports com.zernova.runtime.core.internal.streaming.object.iterator to
      com.mulesoft.mule.runtime.batch;
  exports com.zernova.runtime.core.internal.time to
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.core.internal.transaction to
      com.zernova.runtime.core.components,
      com.zernova.runtime.extensions.mule.support,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config,
      com.mulesoft.mule.runtime.bti,
      com.mulesoft.mule.runtime.xa,
      spring.beans;
  exports com.zernova.runtime.core.internal.transaction.xa to
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config,
      com.mulesoft.mule.runtime.bti,
      com.mulesoft.mule.runtime.cluster,
      com.mulesoft.mule.runtime.xa,
      spring.beans;
  exports com.zernova.runtime.core.internal.transformer to
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.core.internal.transformer.compression to
      com.mulesoft.mule.runtime.kryo;
  exports com.zernova.runtime.core.internal.transformer.datatype to
      spring.beans;
  exports com.zernova.runtime.core.internal.transformer.simple to
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config,
      spring.beans;
  exports com.zernova.runtime.core.internal.util to
      com.zernova.runtime.core.components,
      com.zernova.runtime.container,
      com.zernova.runtime.deployment.model,
      com.zernova.runtime.deployment.model.impl,
      com.zernova.runtime.log4j,
      com.zernova.runtime.service,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.extensions.spring.support,
      com.zernova.runtime.extensions.xml.support,
      com.zernova.runtime.repository,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.tooling.support,
      com.mulesoft.mule.runtime.bti,
      com.mulesoft.mule.runtime.cluster,
      com.mulesoft.mule.runtime.kryo,
      com.mulesoft.mule.runtime.plugin,
      com.mulesoft.mule.service.oauth.ee,
      com.mulesoft.test.mule.service.oauth.ee,
      spring.beans;
  exports com.zernova.runtime.core.internal.util.attribute to
      com.zernova.runtime.core.components,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.extensions.mule.support,
      com.mulesoft.mule.runtime.cache,
      com.mulesoft.mule.runtime.core.ee;
  exports com.zernova.runtime.core.internal.util.cache to
      com.zernova.runtime.metadata.support;
  exports com.zernova.runtime.core.internal.util.collection to
      com.zernova.runtime.extensions.support;
  exports com.zernova.runtime.core.internal.util.log to
      com.mulesoft.anypoint.gw.core;
  exports com.zernova.runtime.core.internal.util.mediatype to
      com.zernova.runtime.extensions.support;
  exports com.zernova.runtime.core.internal.util.message to
      com.zernova.runtime.extensions.support;
  exports com.zernova.runtime.core.internal.util.message.stream to
      com.zernova.runtime.extensions.support;
  exports com.zernova.runtime.core.internal.util.rx to
      com.zernova.runtime.core.components,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.extensions.xml.support,
      com.zernova.runtime.spring.config,
      com.mulesoft.mule.runtime.core.ee,
      com.mulesoft.mule.runtime.batch,
      com.mulesoft.mule.runtime.cache;
  // TODO W-13824979 Remove splashScreen logic from mule-core
  exports com.zernova.runtime.core.internal.util.splash to
      com.zernova.runtime.launcher,
      com.zernova.runtime.service,
      com.zernova.runtime.deployment,
      com.zernova.runtime.deployment.model.impl,
      com.mulesoft.mule.runtime.batch,
      com.mulesoft.mule.runtime.cluster;
  exports com.zernova.runtime.core.internal.util.queue to
      com.zernova.runtime.spring.config,
      com.mulesoft.mule.runtime.bti,
      com.mulesoft.mule.runtime.cluster,
      spring.beans;
  exports com.zernova.runtime.core.internal.util.store to
      com.zernova.runtime.spring.config,
      com.zernova.runtime.tooling.support,
      com.mulesoft.mule.runtime.cluster,
      com.mulesoft.mule.runtime.kryo,
      spring.beans;
  exports com.zernova.runtime.core.internal.util.version to
      com.zernova.runtime.extensions.support,
      com.mulesoft.mule.runtime.license,
      com.mulesoft.anypoint.gw.core;
  exports com.zernova.runtime.core.internal.value.cache to
      com.zernova.runtime.metadata.support,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.tooling.support;

  opens com.zernova.runtime.core.api to
      spring.core;
  opens com.zernova.runtime.core.api.config to
      spring.core;
  opens com.zernova.runtime.core.api.policy to
      spring.core;
  opens com.zernova.runtime.core.api.context.notification to
      kryo.shaded;
  opens com.zernova.runtime.core.api.management.stats to
      kryo.shaded;
  opens com.zernova.runtime.core.api.processor to
      spring.core;
  opens com.zernova.runtime.core.api.retry.policy to
      spring.core;
  opens com.zernova.runtime.core.api.retry.async to
      spring.core;
  opens com.zernova.runtime.core.api.security to
      spring.core;
  opens com.zernova.runtime.core.api.streaming to
      spring.core;

  opens com.zernova.runtime.core.privileged.event to
      kryo.shaded;
  opens com.zernova.runtime.core.privileged.exception to
      kryo.shaded,
      spring.core;
  opens com.zernova.runtime.core.privileged.processor to
      spring.core;
  opens com.zernova.runtime.core.privileged.processor.chain to
      spring.core;
  opens com.zernova.runtime.core.privileged.processor.objectfactory to
      spring.core;
  opens com.zernova.runtime.core.privileged.processor.simple to
      spring.core;
  opens com.zernova.runtime.core.privileged.routing to
      kryo.shaded;

  opens com.zernova.runtime.core.internal.config to
      spring.core;
  opens com.zernova.runtime.core.internal.connection to
      spring.core;
  opens com.zernova.runtime.core.internal.construct to
      spring.core;
  opens com.zernova.runtime.core.internal.context.notification to
      kryo.shaded,
      spring.core;
  opens com.zernova.runtime.core.internal.el.function to
      spring.core;
  opens com.zernova.runtime.core.internal.exception to
      kryo.shaded,
      spring.core;
  opens com.zernova.runtime.core.internal.execution to
      spring.core;
  opens com.zernova.runtime.core.internal.event to
      com.mulesoft.mule.runtime.kryo,
      kryo.shaded;
  opens com.zernova.runtime.core.internal.lock to
      com.zernova.runtime.core.components,
      spring.core;
  opens com.zernova.runtime.core.internal.management.stats to
      kryo.shaded;
  opens com.zernova.runtime.core.internal.message to
      com.mulesoft.mule.runtime.kryo,
      kryo.shaded;
  opens com.zernova.runtime.core.internal.policy to
      com.zernova.runtime.deployment,
      spring.core;
  opens com.zernova.runtime.core.internal.processor.interceptor to
      spring.core;
  opens com.zernova.runtime.core.internal.processor.strategy to
      spring.core;
  opens com.zernova.runtime.core.internal.profiling to
      spring.core;
  opens com.zernova.runtime.core.internal.streaming to
      kryo.shaded,
      spring.core;
  opens com.zernova.runtime.core.internal.streaming.object to
      kryo.shaded;
  opens com.zernova.runtime.core.internal.transaction to
      spring.core;
  opens com.zernova.runtime.core.internal.transformer to
      spring.core;

  uses com.zernova.runtime.core.privileged.transaction.TransactionFactory;
  uses com.zernova.runtime.core.api.util.ClassLoaderResourceNotFoundExceptionFactory;
  uses ArtifactClassLoaderFinder;

}
