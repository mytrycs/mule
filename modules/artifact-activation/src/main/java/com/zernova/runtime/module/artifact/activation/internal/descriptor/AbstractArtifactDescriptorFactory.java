/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.internal.descriptor;

import com.zernova.runtime.api.deployment.meta.AbstractZernovaArtifactModel;
import com.zernova.runtime.api.deployment.meta.ZernovaArtifactLoaderDescriptor;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidator;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidatorBuilder;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ClassLoaderConfiguration;

import java.io.File;

/**
 * Base class to create artifact descriptors.
 *
 * @param <M> type of the artifact model that owns the descriptor.
 * @param <T> type of descriptor being created.
 */
public abstract class AbstractArtifactDescriptorFactory<M extends AbstractZernovaArtifactModel, T extends ArtifactDescriptor> {

  private final File artifactLocation;
  private M artifactModel;
  private final ArtifactDescriptorValidator artifactDescriptorValidator;

  public AbstractArtifactDescriptorFactory(File artifactLocation,
                                           ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder) {
    this.artifactLocation = artifactLocation;

    this.artifactDescriptorValidator = artifactDescriptorValidatorBuilder
        .validateMinZernovaVersion()
        .validateMuleProduct()
        .validateVersionFormat()
        .validateSupportedJavaVersions()
        .build();
  }

  public T create() {
    artifactModel = createArtifactModel();
    validateModel(artifactModel);
    final T descriptor = doCreateArtifactDescriptor();

    BundleDescriptor bundleDescriptor = getBundleDescriptor();
    descriptor.setBundleDescriptor(bundleDescriptor);
    descriptor.setMinZernovaVersion(new ZernovaVersion(artifactModel.getMinZernovaVersion()));
    descriptor.setRequiredProduct(artifactModel.getRequiredProduct());

    ClassLoaderConfiguration classLoaderConfiguration =
        getClassLoaderConfiguration(artifactModel.getClassLoaderModelLoaderDescriptor());
    descriptor.setClassLoaderConfiguration(classLoaderConfiguration);

    doDescriptorConfig(descriptor);

    doValidation(descriptor);

    return descriptor;
  }

  private void validateModel(M artifactModel) {
    artifactModel.validateModel(artifactLocation.getName());
  }

  protected void doValidation(T descriptor) {
    artifactDescriptorValidator.validate(descriptor);
  }

  protected M getArtifactModel() {
    return artifactModel;
  }

  public File getArtifactLocation() {
    return artifactLocation;
  }

  protected abstract M createArtifactModel();

  protected abstract void doDescriptorConfig(T descriptor);

  protected abstract ClassLoaderConfiguration getClassLoaderConfiguration(ZernovaArtifactLoaderDescriptor muleArtifactLoaderDescriptor);

  protected abstract BundleDescriptor getBundleDescriptor();

  protected abstract T doCreateArtifactDescriptor();
}
