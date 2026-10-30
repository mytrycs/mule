/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Test module for mule-api
 *
 * @since 1.5
 */

import com.zernova.runtime.api.metadata.AbstractDataTypeBuilderFactory;
import com.zernova.runtime.api.test.metadata.DefaultDataTypeBuilderFactory;

module com.zernova.runtime.api.test {

  requires transitive com.zernova.runtime.api;
  requires transitive com.zernova.runtime.api.annotations;
  requires com.zernova.runtime.metadata.model.api;
  requires com.zernova.runtime.metadata.model.message;
  requires com.zernova.runtime.artifact.declaration;
  requires com.zernova.runtime.artifact.declaration.persistence;

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
  requires net.bytebuddy;
  requires org.json;
  requires jsonassert;

  exports com.zernova.runtime.api.test;
  exports com.zernova.runtime.api.test.alerts;
  exports com.zernova.runtime.api.test.component;
  exports com.zernova.runtime.api.test.component.execution;
  exports com.zernova.runtime.api.test.component.location;
  exports com.zernova.runtime.api.test.el;
  exports com.zernova.runtime.api.test.exception;
  exports com.zernova.runtime.api.test.functional;
  exports com.zernova.runtime.api.test.internal.util.collection;
  exports com.zernova.runtime.api.test.internal.exception;
  exports com.zernova.runtime.api.test.message.error.matcher;
  exports com.zernova.runtime.api.test.meta;
  exports com.zernova.runtime.api.test.meta.model;
  exports com.zernova.runtime.api.test.meta.model.connection;
  exports com.zernova.runtime.api.test.meta.model.deprecated;
  exports com.zernova.runtime.api.test.meta.model.declaration.fuent;
  exports com.zernova.runtime.api.test.meta.model.tck;
  exports com.zernova.runtime.api.test.meta.model.util;
  exports com.zernova.runtime.api.test.metadata;
  exports com.zernova.runtime.api.test.metadata.descriptor;
  exports com.zernova.runtime.api.test.persistence;
  exports com.zernova.runtime.api.test.scheduler;
  exports com.zernova.runtime.api.test.store;
  exports com.zernova.runtime.api.test.streaming;
  exports com.zernova.runtime.api.test.util;
  exports com.zernova.runtime.api.test.util.collection;
  exports com.zernova.runtime.api.test.util.tck;
  exports com.zernova.runtime.api.test.util.xmlsecurity;

  opens com.zernova.runtime.api.test.exception to org.mockito;
  opens com.zernova.runtime.api.test.meta.model.util to org.mockito;

  provides AbstractDataTypeBuilderFactory with DefaultDataTypeBuilderFactory;
}
