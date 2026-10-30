/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.domain;

import static com.zernova.runtime.api.artifact.ArtifactType.DOMAIN;
import static com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor.DEFAULT_CONFIGURATION_RESOURCE;

import com.zernova.runtime.api.artifact.ArtifactType;
import com.zernova.runtime.api.deployment.meta.ZernovaDomainModel;
import com.zernova.runtime.api.deployment.persistence.AbstractZernovaArtifactModelJsonSerializer;
import com.zernova.runtime.api.deployment.persistence.ZernovaDomainModelJsonSerializer;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidatorBuilder;
import com.zernova.runtime.module.artifact.api.descriptor.DescriptorLoaderRepository;
import com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor;
import com.zernova.runtime.module.deployment.impl.internal.artifact.AbstractDeployableDescriptorFactory;
import com.zernova.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorLoader;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

/**
 * Creates artifact descriptor for application
 */
public class DomainDescriptorFactory extends AbstractDeployableDescriptorFactory<ZernovaDomainModel, DomainDescriptor> {

  public DomainDescriptorFactory(ArtifactPluginDescriptorLoader artifactPluginDescriptorLoader,
                                 DescriptorLoaderRepository descriptorLoaderRepository,
                                 ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder) {
    super(artifactPluginDescriptorLoader, descriptorLoaderRepository, artifactDescriptorValidatorBuilder);
  }


  @Override
  protected String getDefaultConfigurationResource() {
    return DEFAULT_CONFIGURATION_RESOURCE;
  }

  @Override
  protected ArtifactType getArtifactType() {
    return DOMAIN;
  }

  @Override
  protected AbstractZernovaArtifactModelJsonSerializer<ZernovaDomainModel> getMuleArtifactModelJsonSerializer() {
    return new ZernovaDomainModelJsonSerializer();
  }

  @Override
  protected void doDescriptorConfig(ZernovaDomainModel artifactModel, DomainDescriptor descriptor, File artifactLocation) {
    super.doDescriptorConfig(artifactModel, descriptor, artifactLocation);
  }

  @Override
  protected DomainDescriptor createArtifactDescriptor(File artifactLocation, String name,
                                                      Optional<Properties> deploymentProperties) {
    // Keep compatibility with usages of the factory that expect the descriptor from previous version.
    return new com.zernova.runtime.deployment.model.api.domain.DomainDescriptor(artifactLocation.getName(), deploymentProperties);
  }
}
