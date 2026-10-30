/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.api.builder;

import com.zernova.runtime.deployment.model.api.application.Application;
import com.zernova.runtime.deployment.model.api.domain.Domain;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaDeployableArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;

/**
 * {@link ArtifactClassLoader} builder for class loaders required by {@link Application} artifacts
 *
 * @since 4.5
 */
public interface ApplicationClassLoaderBuilder {

  /**
   * @param domainArtifactClassLoader the {@link ArtifactClassLoader} of the {@link Domain} to which the application that is going
   *                                  to use the target classloader belongs.
   * @return the builder
   */
  ApplicationClassLoaderBuilder setDomainParentClassLoader(ArtifactClassLoader domainArtifactClassLoader);

  /**
   * @param artifactDescriptor the descriptor of the artifact for which the class loader is going to be created.
   * @return the builder
   */
  ApplicationClassLoaderBuilder setArtifactDescriptor(ArtifactDescriptor artifactDescriptor);

  /**
   * Creates a new {@code ZernovaDeployableArtifactClassLoader} using the provided configuration. It will create the proper class
   * loader hierarchy and filters so that application classes, resources, plugins and its domain resources are resolve correctly.
   *
   * @return a {@code ZernovaDeployableArtifactClassLoader} created from the provided configuration.
   */
  ZernovaDeployableArtifactClassLoader build();

}
