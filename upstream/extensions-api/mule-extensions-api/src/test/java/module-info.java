/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Test module for mule-extensions-api
 *
 * @since 1.5
 */
module com.zernova.runtime.extensions.api.test {

  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.metadata.model.api;
  requires com.zernova.runtime.api.test;
  requires com.zernova.runtime.metadata.model.java;
  requires com.zernova.sdk.api;

  requires jakarta.activation;

  requires com.github.benmanes.caffeine;
  requires com.google.common;
  requires com.google.gson;
  requires org.apache.commons.io;
  requires org.apache.commons.lang3;
  requires semver4j;
  requires transitive junit;
  requires org.hamcrest;
  requires io.qameta.allure.commons;
  requires org.mockito;
  requires net.bytebuddy.agent;
  requires org.json;
  requires jsonassert;

  exports com.zernova.runtime.extension.api.test;
  exports com.zernova.runtime.extension.api.test.declaration;
  exports com.zernova.runtime.extension.api.test.declaration.fluent;
  exports com.zernova.runtime.extension.api.test.declaration.fluent.util;
  exports com.zernova.runtime.extension.api.test.declaration.type;
  exports com.zernova.runtime.extension.api.test.declaration.type.annotation;
  exports com.zernova.runtime.extension.api.test.dsl;
  exports com.zernova.runtime.extension.api.test.dsl.model;
  exports com.zernova.runtime.extension.api.test.internal;
  exports com.zernova.runtime.extension.api.test.internal.loader;
  exports com.zernova.runtime.extension.api.test.internal.loader.declaration.type.annotation;
  exports com.zernova.runtime.extension.api.test.internal.loader.enricher;
  exports com.zernova.runtime.extension.api.test.internal.loader.util;
  exports com.zernova.runtime.extension.api.test.internal.loader.validator;
  exports com.zernova.runtime.extension.api.test.internal.semantic;
  exports com.zernova.runtime.extension.api.test.mimetype;
  exports com.zernova.runtime.extension.api.test.runtime;
  exports com.zernova.runtime.extension.api.test.runtime.operation;
  exports com.zernova.runtime.extension.api.test.runtime.parameters;
  exports com.zernova.runtime.extension.api.test.stereotype;
  exports com.zernova.runtime.extension.api.test.util;

}
