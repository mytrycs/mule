/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.internal.domain;

import com.zernova.runtime.deployment.model.api.builder.DomainClassLoaderBuilder;
import com.zernova.runtime.deployment.model.api.domain.Domain;
import com.zernova.runtime.module.artifact.activation.api.classloader.ArtifactClassLoaderResolver;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaDeployableArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor;

/**
 * {@link ArtifactClassLoader} builder for class loaders required by {@link Domain} artifacts
 *
 * @since 4.0
 */
public class DefaultDomainClassLoaderBuilder implements DomainClassLoaderBuilder {

  private ArtifactDescriptor artifactDescriptor;
  private final ArtifactClassLoaderResolver artifactClassLoaderResolver;

  /**
   * Creates a new builder for creating {@link Domain} artifacts.
   *
   * @param artifactClassLoaderResolver resolver that will be used to create the class loader. Non-null
   */
  public DefaultDomainClassLoaderBuilder(ArtifactClassLoaderResolver artifactClassLoaderResolver) {
    this.artifactClassLoaderResolver = artifactClassLoaderResolver;
  }

  @Override
  public DomainClassLoaderBuilder setArtifactDescriptor(ArtifactDescriptor artifactDescriptor) {
    this.artifactDescriptor = artifactDescriptor;
    return this;
  }

  /**
   * Creates a new {@code ArtifactClassLoader} using the provided configuration. It will create the proper class loader hierarchy
   * and filters so domain classes, resources and plugins are resolve correctly.
   *
   * @return a {@code ZernovaDeployableArtifactClassLoader} created from the provided configuration.
   */
  @Override
  public ZernovaDeployableArtifactClassLoader build() {
    return artifactClassLoaderResolver
        .createDomainClassLoader((DomainDescriptor) artifactDescriptor);
  }
}
