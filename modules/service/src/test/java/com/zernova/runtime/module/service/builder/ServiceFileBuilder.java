/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.service.builder;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyMap;
import static com.zernova.runtime.api.deployment.meta.Product.ZERNOVA;
import static com.zernova.runtime.core.api.util.FileUtils.unzip;
import static com.zernova.runtime.core.api.util.StringUtils.isBlank;
import static com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor.REPOSITORY_FOLDER;
import static com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor.MULE_ARTIFACT_JSON_DESCRIPTOR_LOCATION;
import static com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorConstants.MULE_LOADER_ID;

import com.zernova.runtime.api.deployment.meta.ZernovaArtifactLoaderDescriptor;
import com.zernova.runtime.api.deployment.meta.ZernovaServiceContractModel;
import com.zernova.runtime.api.deployment.meta.ZernovaServiceModel.ZernovaServiceModelBuilder;
import com.zernova.runtime.api.deployment.persistence.ZernovaServiceModelJsonSerializer;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.module.artifact.builder.AbstractArtifactFileBuilder;
import com.zernova.runtime.module.artifact.builder.AbstractDependencyFileBuilder;
import com.zernova.tck.ZipUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Creates Service files.
 */
public class ServiceFileBuilder extends AbstractArtifactFileBuilder<ServiceFileBuilder> {

  private String serviceProviderClassName;
  private String contractClassName = null;
  private boolean unpack = false;

  /**
   * Creates a new builder
   *
   * @param artifactId artifact identifier. Non empty.
   */
  public ServiceFileBuilder(String artifactId) {
    super(artifactId);
  }

  @Override
  protected ServiceFileBuilder getThis() {
    return this;
  }

  public ServiceFileBuilder unpack(boolean unpack) {
    this.unpack = unpack;
    return this;
  }

  /**
   * Configures the service provider
   *
   * @param className service provider class name. Non blank.
   * @return the same builder instance
   */
  public ServiceFileBuilder withServiceProviderClass(String className) {
    checkImmutable();
    checkArgument(!isBlank(className), "Property value cannot be blank");
    serviceProviderClassName = className;

    return this;
  }

  public ServiceFileBuilder forContract(String contractClassName) {
    checkImmutable();
    checkArgument(!isBlank(contractClassName), "Property value cannot be blank");
    this.contractClassName = contractClassName;

    return this;
  }

  @Override
  protected final List<ZipUtils.ZipResource> getCustomResources() {
    final List<ZipUtils.ZipResource> customResources = new LinkedList<>();

    for (AbstractDependencyFileBuilder dependencyFileBuilder : getAllCompileDependencies()) {
      customResources.add(new ZipUtils.ZipResource(dependencyFileBuilder.getArtifactFile().getAbsolutePath(),
                                                   Paths.get(REPOSITORY_FOLDER,
                                                             dependencyFileBuilder.getArtifactFileRepositoryPath())
                                                       .toString()));

      customResources.add(new ZipUtils.ZipResource(dependencyFileBuilder.getArtifactPomFile().getAbsolutePath(),
                                                   Paths.get(REPOSITORY_FOLDER,
                                                             dependencyFileBuilder.getArtifactFilePomRepositoryPath())
                                                       .toString()));
    }

    File serviceDescriptor = createServiceJsonDescriptorFile();
    customResources.add(new ZipUtils.ZipResource(serviceDescriptor.getAbsolutePath(), MULE_ARTIFACT_JSON_DESCRIPTOR_LOCATION));

    return customResources;
  }

  private File createServiceJsonDescriptorFile() {
    File serviceDescriptor = new File(getTempFolder(), getArtifactId() + "service.json");
    serviceDescriptor.deleteOnExit();
    ZernovaServiceModelBuilder serviceModelBuilder = new ZernovaServiceModelBuilder()
        .setName(getArtifactId())
        .setMinZernovaVersion("4.2.0")
        .setRequiredProduct(ZERNOVA)
        .withClassLoaderModelDescriptorLoader(new ZernovaArtifactLoaderDescriptor(MULE_LOADER_ID, emptyMap()))
        .withBundleDescriptorLoader(new ZernovaArtifactLoaderDescriptor(MULE_LOADER_ID, emptyMap()))
        .withContracts(Arrays.asList(new ZernovaServiceContractModel(serviceProviderClassName, contractClassName)));

    String serviceDescriptorContent = new ZernovaServiceModelJsonSerializer().serialize(serviceModelBuilder.build());
    try (FileWriter fileWriter = new FileWriter(serviceDescriptor)) {
      fileWriter.write(serviceDescriptorContent);
    } catch (IOException e) {
      throw new ZernovaRuntimeException(e);
    }
    return serviceDescriptor;
  }

  @Override
  public String getConfigFile() {
    return null;
  }

  @Override
  public File getArtifactFile() {
    boolean newlyCreated = artifactFile == null;
    File file = super.getArtifactFile();
    if (unpack && newlyCreated && !file.isDirectory()) {
      File unpacked = new File(file.getParentFile(), file.getName() + "-unpack");
      unpacked.mkdirs();
      try {
        unzip(file, unpacked, false);
      } catch (IOException e) {
        throw new ZernovaRuntimeException(e);
      }

      file.delete();
      final File newFile = new File(file.getParentFile(), file.getName().replaceAll(".jar", ""));
      unpacked.renameTo(newFile);
      file = new File(newFile.getAbsolutePath());
      artifactFile = file;
    }

    return file;
  }
}
