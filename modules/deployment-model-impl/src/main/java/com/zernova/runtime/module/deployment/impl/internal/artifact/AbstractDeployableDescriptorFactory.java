/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.artifact;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.container.api.ZernovaFoldersUtil.getMuleHomeFolder;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

import com.zernova.runtime.api.deployment.meta.ZernovaDeployableModel;
import com.zernova.runtime.module.artifact.api.descriptor.AbstractArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidatorBuilder;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDependency;
import com.zernova.runtime.module.artifact.api.descriptor.DeployableArtifactDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.DescriptorLoaderRepository;
import com.zernova.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDeployableDescriptorFactory<M extends ZernovaDeployableModel, T extends DeployableArtifactDescriptor>
    extends AbstractArtifactDescriptorFactory<M, T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDeployableDescriptorFactory.class);

  private final ArtifactPluginDescriptorLoader artifactPluginDescriptorLoader;

  public AbstractDeployableDescriptorFactory(ArtifactPluginDescriptorLoader artifactPluginDescriptorLoader,
                                             DescriptorLoaderRepository descriptorLoaderRepository,
                                             ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder) {
    super(descriptorLoaderRepository, artifactDescriptorValidatorBuilder);
    checkArgument(artifactPluginDescriptorLoader != null, "ApplicationPluginDescriptorFactory cannot be null");

    this.artifactPluginDescriptorLoader = artifactPluginDescriptorLoader;
  }

  @Override
  protected void doDescriptorConfig(M artifactModel, T descriptor, File artifactLocation) {
    descriptor.setArtifactLocation(artifactLocation);
    descriptor.setRedeploymentEnabled(artifactModel.isRedeploymentEnabled());
    Set<String> configs = artifactModel.getConfigs();
    if (configs != null && !configs.isEmpty()) {
      descriptor.setConfigResources(configs.stream()
          .collect(toSet()));
    } else {
      descriptor.setConfigResources(ImmutableSet.<String>builder().add(getDefaultConfigurationResource()).build());
    }

    try {
      descriptor.setPlugins(createArtifactPluginDescriptors(descriptor));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    descriptor.setLogConfigFile(getLogConfigFile(artifactModel));

    descriptor.setSupportedJavaVersions(artifactModel.getSupportedJavaVersions());
  }

  protected File getLogConfigFile(M artifactModel) {
    File logConfigFile = null;
    if (artifactModel.getLogConfigFile() != null) {
      Path logConfigFilePath = new File(artifactModel.getLogConfigFile()).toPath();
      Path muleHomeFolderPath = getMuleHomeFolder().toPath();
      logConfigFile = muleHomeFolderPath.resolve(logConfigFilePath).toFile();
    }
    return logConfigFile;
  }

  protected abstract String getDefaultConfigurationResource();

  private Set<ArtifactPluginDescriptor> createArtifactPluginDescriptors(T descriptor)
      throws IOException {
    Set<ArtifactPluginDescriptor> pluginDescriptors = new HashSet<>();
    for (BundleDependency bundlePluginDependency : descriptor.getClassLoaderConfiguration().getDependencies()) {
      if (bundlePluginDependency.getDescriptor().isPlugin()) {
        if (bundlePluginDependency.getBundleUri() == null) {
          LOGGER
              .warn(format("Plugin '%s' is declared as 'provided' which means that it will not be added to the artifact's classpath",
                           bundlePluginDependency.getDescriptor()));
        } else {
          File pluginFile = new File(bundlePluginDependency.getBundleUri());
          pluginDescriptors
              .add(artifactPluginDescriptorLoader.load(pluginFile, bundlePluginDependency.getDescriptor(), descriptor));
        }
      }
    }
    return pluginDescriptors;
  }
}
