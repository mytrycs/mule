/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Mule Deployment Model Implementation Module.
 *
 * @moduleGraph
 * @since 4.6
 */
open module com.zernova.runtime.deployment.test {

  requires com.zernova.runtime.deployment;
  requires com.zernova.test.services;
  requires com.zernova.weave.mule.dwb.api;

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.artifact;
  requires com.zernova.runtime.artifact.activation;
  requires com.zernova.runtime.artifact.ast;
  requires com.zernova.runtime.artifact.ast.serialization;
  requires com.zernova.runtime.artifact.ast.xmlParser;
  requires com.zernova.runtime.artifact.declaration;
  requires com.zernova.runtime.container;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.core.components;
  requires com.zernova.runtime.deployment.model;
  requires com.zernova.runtime.deployment.model.impl;
  requires com.zernova.runtime.extension.model;
  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.extensions.spring.support;
  requires com.zernova.runtime.global.config;
  requires com.zernova.runtime.maven.client.api;
  requires com.zernova.runtime.policy.api;
  requires com.zernova.runtime.properties.config;
  requires com.zernova.runtime.spring.config;
  requires com.zernova.sdk.api;
  requires com.zernova.runtime.jpms.utils;

  requires com.zernova.runtime.service;
  requires com.zernova.runtime.extensions.xml.support;
  requires com.zernova.runtime.license.api;

  requires com.zernova.test.runner;
  requires com.zernova.runtime.deployment.model.impl.test;
  requires com.zernova.runtime.oauth.api;
  requires com.zernova.oauth.client.api;

  requires org.apache.logging.log4j;
  requires org.apache.commons.io;
  requires org.apache.commons.lang3;
  requires org.mockito;
  requires org.jetbrains.annotations;
  requires io.qameta.allure.commons;

}
