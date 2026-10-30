/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.service.internal.artifact;

import static com.zernova.runtime.api.artifact.ArtifactType.SERVICE;

import com.zernova.runtime.api.artifact.ArtifactType;
import com.zernova.runtime.api.deployment.meta.ZernovaServiceModel;
import com.zernova.runtime.api.deployment.persistence.AbstractZernovaArtifactModelJsonSerializer;
import com.zernova.runtime.api.deployment.persistence.ZernovaServiceModelJsonSerializer;
import com.zernova.runtime.module.artifact.api.descriptor.AbstractArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorCreateException;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidator;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidatorBuilder;
import com.zernova.runtime.module.artifact.api.descriptor.ClassLoaderConfigurationLoader;
import com.zernova.runtime.module.artifact.api.descriptor.DescriptorLoaderRepository;
import com.zernova.runtime.module.service.api.artifact.ServiceDescriptor;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

/**
 * Creates {@link ServiceDescriptor} instances.
 */
public class ServiceDescriptorFactory extends AbstractArtifactDescriptorFactory<ZernovaServiceModel, ServiceDescriptor> {

  /**
   * Creates a new factory
   *
   * @param descriptorLoaderRepository         contains all the {@link ClassLoaderConfigurationLoader} registered on the
   *                                           container. Non null
   * @param artifactDescriptorValidatorBuilder {@link ArtifactDescriptorValidatorBuilder} to create the
   *                                           {@link ArtifactDescriptorValidator} in order to check the state of the descriptor
   *                                           once loaded.
   */
  public ServiceDescriptorFactory(DescriptorLoaderRepository descriptorLoaderRepository,
                                  ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder) {
    super(descriptorLoaderRepository,
          artifactDescriptorValidatorBuilder.doNotFailIfBundleDescriptorNotPresentWhenValidationVersionFormat());
  }

  @Override
  protected ArtifactType getArtifactType() {
    return SERVICE;
  }

  @Override
  public ServiceDescriptor create(File artifactFolder, Optional<Properties> properties) throws ArtifactDescriptorCreateException {
    if (!artifactFolder.exists()) {
      throw new IllegalArgumentException("Service folder does not exists: " + artifactFolder.getAbsolutePath());
    }

    return super.create(artifactFolder, properties);
  }

  @Override
  protected void doDescriptorConfig(ZernovaServiceModel artifactModel, ServiceDescriptor descriptor, File artifactLocation) {
    descriptor.setContractModels(artifactModel.getContracts());
  }

  @Override
  protected ServiceDescriptor createArtifactDescriptor(File artifactLocation, String name, Optional<Properties> properties) {
    return new ServiceDescriptor(name);
  }

  @Override
  protected AbstractZernovaArtifactModelJsonSerializer<ZernovaServiceModel> getMuleArtifactModelJsonSerializer() {
    return new ZernovaServiceModelJsonSerializer();
  }
}
