/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.plugin;

import static com.zernova.runtime.api.artifact.ArtifactType.PLUGIN;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.core.internal.util.jar.JarLoadingUtils.loadFileContentFrom;
import static com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor.MULE_ARTIFACT_JSON_DESCRIPTOR;
import static com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor.MULE_ARTIFACT_PATH_INSIDE_JAR;

import static java.lang.String.format;

import com.zernova.runtime.api.artifact.ArtifactType;
import com.zernova.runtime.api.deployment.meta.ZernovaArtifactLoaderDescriptor;
import com.zernova.runtime.api.deployment.meta.ZernovaPluginModel;
import com.zernova.runtime.api.deployment.persistence.AbstractZernovaArtifactModelJsonSerializer;
import com.zernova.runtime.api.deployment.persistence.ZernovaPluginModelJsonSerializer;
import com.zernova.runtime.deployment.model.api.artifact.DescriptorLoaderRepositoryFactory;
import com.zernova.runtime.module.artifact.api.descriptor.AbstractArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorCreateException;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidatorBuilder;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ClassLoaderConfigurationLoader;
import com.zernova.runtime.module.artifact.api.descriptor.DescriptorLoaderRepository;
import com.zernova.runtime.module.artifact.api.plugin.LoaderDescriber;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Creates {@link ArtifactPluginDescriptor} instances
 */
public class ArtifactPluginDescriptorFactory
    extends AbstractArtifactDescriptorFactory<ZernovaPluginModel, ArtifactPluginDescriptor> {

  /**
   * Creates a default factory
   */
  public ArtifactPluginDescriptorFactory() {
    this(new DescriptorLoaderRepositoryFactory().createDescriptorLoaderRepository(),
         ArtifactDescriptorValidatorBuilder.builder());
  }

  /**
   * Creates a custom factory
   *
   * @param descriptorLoaderRepository         contains all the {@link ClassLoaderConfigurationLoader} registered on the
   *                                           container. Non null
   * @param artifactDescriptorValidatorBuilder {@link ArtifactDescriptorValidatorBuilder} builder to define the validator to be
   *                                           used. Non null.
   */
  public ArtifactPluginDescriptorFactory(DescriptorLoaderRepository descriptorLoaderRepository,
                                         ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder) {
    super(descriptorLoaderRepository, artifactDescriptorValidatorBuilder);
  }

  @Override
  public ArtifactPluginDescriptor create(File pluginJarFile, Optional<Properties> deploymentProperties)
      throws ArtifactDescriptorCreateException {
    try {
      checkArgument(pluginJarFile.isDirectory() || pluginJarFile.getName().endsWith(".jar"),
                    "provided file is not a plugin: " + pluginJarFile.getAbsolutePath());
      // Use / instead of File.separator as the file is going to be accessed inside the jar as a URL
      String mulePluginJsonPathInsideJarFile = MULE_ARTIFACT_PATH_INSIDE_JAR + "/" + MULE_ARTIFACT_JSON_DESCRIPTOR;
      Optional<byte[]> jsonDescriptorContentOptional = loadFileContentFrom(pluginJarFile, mulePluginJsonPathInsideJarFile);
      return jsonDescriptorContentOptional
          .map(jsonDescriptorContent -> loadFromJsonDescriptor(pluginJarFile,
                                                               loadModelFromJson(new String(jsonDescriptorContent)),
                                                               deploymentProperties))
          .orElseThrow(() -> new ArtifactDescriptorCreateException(pluginDescriptorNotFound(pluginJarFile,
                                                                                            mulePluginJsonPathInsideJarFile)));
    } catch (ArtifactDescriptorCreateException e) {
      throw e;
    } catch (IOException e) {
      throw new ArtifactDescriptorCreateException(e);
    }
  }

  @Override
  protected Map<String, Object> getClassLoaderConfigurationAttributes(Optional<Properties> deploymentPropertiesOptional,
                                                                      ZernovaArtifactLoaderDescriptor classLoaderModelLoaderDescriptor,
                                                                      BundleDescriptor bundleDescriptor) {
    Map<String, Object> attributes =
        super.getClassLoaderConfigurationAttributes(deploymentPropertiesOptional, classLoaderModelLoaderDescriptor,
                                                    bundleDescriptor);

    if (deploymentPropertiesOptional.isPresent()) {
      Properties deploymentProperties = deploymentPropertiesOptional.get();
      if (deploymentProperties instanceof PluginExtendedDeploymentProperties) {
        PluginExtendedDeploymentProperties pluginExtendedDeploymentProperties =
            (PluginExtendedDeploymentProperties) deploymentProperties;
        return new PluginExtendedClassLoaderConfigurationAttributes(attributes,
                                                                    pluginExtendedDeploymentProperties
                                                                        .getDeployableArtifactDescriptor());
      }
    }
    return attributes;
  }

  @Override
  protected Map<String, Object> getBundleDescriptorAttributes(ZernovaArtifactLoaderDescriptor bundleDescriptorLoader,
                                                              Optional<Properties> deploymentPropertiesOptional) {
    Map<String, Object> attributes =
        super.getBundleDescriptorAttributes(bundleDescriptorLoader, deploymentPropertiesOptional);

    if (deploymentPropertiesOptional.isPresent()) {
      Properties deploymentProperties = deploymentPropertiesOptional.get();
      if (deploymentProperties instanceof PluginExtendedDeploymentProperties) {
        PluginExtendedDeploymentProperties pluginExtendedDeploymentProperties =
            (PluginExtendedDeploymentProperties) deploymentProperties;
        return new PluginExtendedBundleDescriptorAttributes(attributes,
                                                            pluginExtendedDeploymentProperties.getPluginBundleDescriptor());
      }
    }
    return attributes;
  }

  @Override
  protected ArtifactType getArtifactType() {
    return PLUGIN;
  }

  @Override
  protected void doDescriptorConfig(ZernovaPluginModel artifactModel, ArtifactPluginDescriptor descriptor, File artifactLocation) {
    artifactModel.getExtensionModelLoaderDescriptor().ifPresent(extensionModelDescriptor -> {
      final LoaderDescriber loaderDescriber = new LoaderDescriber(extensionModelDescriptor.getId());
      loaderDescriber.addAttributes(extensionModelDescriptor.getAttributes());
      descriptor.setExtensionModelDescriptorProperty(loaderDescriber);
    });
    artifactModel.getLicense().ifPresent(descriptor::setLicenseModel);
  }

  @Override
  protected ArtifactPluginDescriptor createArtifactDescriptor(File artifactLocation, String name,
                                                              Optional<Properties> deploymentProperties) {
    return new ArtifactPluginDescriptor(name);
  }

  private static String pluginDescriptorNotFound(File pluginFile, String mulePluginJsonPathInsideJarFile) {
    return format("The plugin descriptor '%s' on plugin file '%s' is not present", mulePluginJsonPathInsideJarFile, pluginFile);
  }

  @Override
  protected AbstractZernovaArtifactModelJsonSerializer<ZernovaPluginModel> getMuleArtifactModelJsonSerializer() {
    return new ZernovaPluginModelJsonSerializer();
  }

}
