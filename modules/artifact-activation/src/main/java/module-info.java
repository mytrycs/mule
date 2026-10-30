/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
import com.zernova.runtime.core.internal.artifact.ArtifactClassLoaderFinder;
import com.zernova.runtime.module.artifact.activation.internal.classloader.DefaultArtifactClassLoaderFinder;

/**
 * Provides behavior necessary for loading artifacts.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.artifact.activation {

  requires transitive com.zernova.runtime.api;
  requires transitive com.zernova.runtime.artifact;
  requires transitive com.zernova.runtime.artifact.ast;
  requires com.zernova.runtime.artifact.ast.xmlParser;
  requires com.zernova.runtime.container;
  requires transitive com.zernova.runtime.core;
  requires com.zernova.runtime.extension.model;
  requires transitive com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.global.config;
  requires com.zernova.runtime.jar.handling.utils;
  requires com.zernova.runtime.jpms.utils;
  requires com.zernova.runtime.maven.client.api;
  requires com.zernova.runtime.metadata.model.api;
  requires com.zernova.runtime.service;
  requires com.zernova.tools.api.classloader;

  requires net.bytebuddy;

  requires org.apache.commons.io;
  requires org.apache.commons.lang3;
  requires org.jgrapht.core;
  requires semver4j;

  requires transitive java.xml;

  exports com.zernova.runtime.module.artifact.activation.api;
  exports com.zernova.runtime.module.artifact.activation.api.ast;
  exports com.zernova.runtime.module.artifact.activation.api.classloader;
  exports com.zernova.runtime.module.artifact.activation.api.deployable;
  exports com.zernova.runtime.module.artifact.activation.api.descriptor;
  exports com.zernova.runtime.module.artifact.activation.api.extension.discovery;
  exports com.zernova.runtime.module.artifact.activation.api.extension.discovery.boot;
  exports com.zernova.runtime.module.artifact.activation.api.plugin;
  exports com.zernova.runtime.module.artifact.activation.api.service.config;
  exports com.zernova.runtime.module.artifact.activation.api.service.discoverer;

  uses com.zernova.runtime.module.artifact.activation.internal.plugin.PluginPatchesResolver;

  exports com.zernova.runtime.module.artifact.activation.internal.ast to
      com.zernova.runtime.extensions.mule.support;
  exports com.zernova.runtime.module.artifact.activation.internal.ast.validation to
      com.zernova.runtime.extensions.mule.support,
      com.zernova.runtime.spring.config;
  exports com.zernova.runtime.module.artifact.activation.internal.classloader to
      com.zernova.runtime.deployment,
      com.zernova.runtime.deployment.model,
      com.zernova.runtime.deployment.model.impl,
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.log4j,
      com.zernova.test.runner;
  exports com.zernova.runtime.module.artifact.activation.internal.deployable to
      com.zernova.runtime.deployment.model.impl,
      com.zernova.runtime.tooling.support;
  exports com.zernova.runtime.module.artifact.activation.internal.descriptor to
      com.zernova.runtime.deployment.model.impl;
  exports com.zernova.runtime.module.artifact.activation.internal.maven to
      com.zernova.runtime.deployment.model.impl;
  exports com.zernova.runtime.module.artifact.activation.internal.nativelib to
      com.zernova.runtime.deployment,
      com.zernova.runtime.deployment.model,
      com.zernova.runtime.deployment.model.impl,
      com.zernova.test.runner;
  exports com.zernova.runtime.module.artifact.activation.internal.plugin to
      com.zernova.runtime.deployment.model.impl;

  provides ArtifactClassLoaderFinder
      with DefaultArtifactClassLoaderFinder;

}
