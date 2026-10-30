/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * test module with the {@code requires} clauses needed only for the tests but not the productive code.
 * @since 1.5
 */
module com.zernova.runtime.extensions.api.persistence.test {

  requires com.zernova.runtime.extensions.api.persistence;

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.api.test;
  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.extensions.api.test;
  requires com.zernova.runtime.metadata.model.api;
  requires com.zernova.runtime.metadata.model.java;
  requires com.zernova.runtime.metadata.model.json;
  requires com.zernova.runtime.metadata.model.persistence;

  requires com.google.gson;
  requires net.bytebuddy;
  requires net.bytebuddy.agent;
  requires com.google.common;

  requires junit;
  requires org.hamcrest;
  requires org.apache.commons.io;
  requires jsonassert;
  requires org.mockito;

  exports com.zernova.runtime.extension.api.persistence.test to
      com.google.gson;

  opens com.zernova.runtime.extension.api.persistence.test to
      junit;
}
