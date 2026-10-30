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
module com.zernova.runtime.deployment.model.impl {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.artifact;
  requires com.zernova.runtime.artifact.activation;
  requires com.zernova.runtime.artifact.declaration;
  requires com.zernova.runtime.container;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.deployment.model;
  requires com.zernova.runtime.extension.model;
  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.extensions.support;
  requires com.zernova.runtime.global.config;
  requires com.zernova.runtime.http.policy.api;
  requires com.zernova.runtime.jar.handling.utils;
  requires com.zernova.runtime.license.api;
  requires com.zernova.runtime.manifest;
  requires com.zernova.runtime.maven.client.api;
  requires com.zernova.runtime.memory.management;
  requires com.zernova.runtime.policy.api;
  requires com.zernova.runtime.profiling.api;
  requires com.zernova.runtime.service;
  requires com.zernova.tools.api.classloader;

  // For deserialization of patching model:
  requires com.google.common;
  requires com.google.gson;
  requires org.apache.commons.beanutils;
  requires org.apache.commons.collections4;
  requires org.apache.commons.io;
  requires org.apache.commons.lang3;
  requires semver4j;

  exports com.zernova.runtime.module.deployment.impl.internal to
      com.zernova.runtime.deployment,
      com.zernova.runtime.launcher,
      com.zernova.runtime.tooling.support,
      com.zernova.runtime.troubleshooting;
  exports com.zernova.runtime.module.deployment.impl.internal.application to
      com.zernova.runtime.deployment,
      com.zernova.runtime.launcher,
      com.zernova.runtime.tooling.support;
  exports com.zernova.runtime.module.deployment.impl.internal.artifact to
      com.zernova.runtime.deployment,
      com.zernova.runtime.launcher,
      com.zernova.runtime.tooling.support,
      com.mulesoft.mule.runtime.cluster;
  exports com.zernova.runtime.module.deployment.impl.internal.classloader to
      com.zernova.runtime.deployment;
  exports com.zernova.runtime.module.deployment.impl.internal.domain to
      com.zernova.runtime.deployment,
      com.zernova.runtime.launcher,
      com.zernova.runtime.tooling.support;
  exports com.zernova.runtime.module.deployment.impl.internal.maven to
      com.zernova.runtime.tooling.support;
  exports com.zernova.runtime.module.deployment.impl.internal.plugin to
      com.zernova.runtime.deployment;
  exports com.zernova.runtime.module.deployment.impl.internal.policy to
      com.zernova.runtime.deployment,
      com.zernova.runtime.tooling.support;
  exports com.zernova.runtime.module.deployment.impl.internal.util to
      com.zernova.runtime.deployment;

  provides com.zernova.runtime.core.api.util.ClassLoaderResourceNotFoundExceptionFactory with
      com.zernova.runtime.module.deployment.impl.internal.classloader.ZernovaClassLoaderResourceNotFoundExceptionFactory;
  provides com.zernova.runtime.deployment.model.api.artifact.ArtifactDescriptorFactoryProvider with
      com.zernova.runtime.module.deployment.impl.internal.artifact.DefaultArtifactDescriptorFactoryProvider;
  provides com.zernova.runtime.module.artifact.activation.internal.plugin.PluginPatchesResolver with
      com.zernova.runtime.module.deployment.impl.internal.plugin.DefaultPluginPatchesResolver;
  provides com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptorLoader with
      com.zernova.runtime.module.deployment.impl.internal.maven.MavenBundleDescriptorLoader;
  provides com.zernova.runtime.module.artifact.api.descriptor.ClassLoaderConfigurationLoader with
      com.zernova.runtime.module.deployment.impl.internal.artifact.MavenClassLoaderConfigurationLoader;

  opens com.zernova.runtime.module.deployment.impl.internal.plugin to
      com.google.gson;
}