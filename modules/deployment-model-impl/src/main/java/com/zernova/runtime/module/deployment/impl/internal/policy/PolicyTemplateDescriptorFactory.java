/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.policy;

import static com.zernova.runtime.api.artifact.ArtifactType.POLICY;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import static java.util.stream.Collectors.toSet;

import com.zernova.runtime.api.artifact.ArtifactType;
import com.zernova.runtime.api.deployment.meta.ZernovaPolicyModel;
import com.zernova.runtime.api.deployment.persistence.AbstractZernovaArtifactModelJsonSerializer;
import com.zernova.runtime.api.deployment.persistence.ZernovaPolicyModelJsonSerializer;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.deployment.model.api.artifact.DescriptorLoaderRepositoryFactory;
import com.zernova.runtime.deployment.model.api.policy.PolicyTemplateDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.AbstractArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidator;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidatorBuilder;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDependency;
import com.zernova.runtime.module.artifact.api.descriptor.ClassLoaderConfigurationLoader;
import com.zernova.runtime.module.artifact.api.descriptor.DescriptorLoaderRepository;
import com.zernova.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorFactory;
import com.zernova.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorLoader;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * Creates descriptors for policy templates
 */
public class PolicyTemplateDescriptorFactory
    extends AbstractArtifactDescriptorFactory<ZernovaPolicyModel, PolicyTemplateDescriptor> {

  private final ArtifactPluginDescriptorLoader artifactPluginDescriptorLoader;

  /**
   * Creates a default factory
   */
  @SuppressWarnings({"unused"})
  public PolicyTemplateDescriptorFactory() {
    this(new ArtifactPluginDescriptorLoader(new ArtifactPluginDescriptorFactory()),
         new DescriptorLoaderRepositoryFactory().createDescriptorLoaderRepository(),
         ArtifactDescriptorValidatorBuilder.builder());
  }

  /**
   * Creates a new factory
   *
   * @param artifactPluginDescriptorLoader     loads the artifact descriptor for plugins used on the policy template. Non null
   * @param descriptorLoaderRepository         contains all the {@link ClassLoaderConfigurationLoader} registered on the
   *                                           container. Non null
   * @param artifactDescriptorValidatorBuilder {@link ArtifactDescriptorValidatorBuilder} to create the
   *                                           {@link ArtifactDescriptorValidator} in order to check the state of the descriptor
   *                                           once loaded.
   */
  public PolicyTemplateDescriptorFactory(ArtifactPluginDescriptorLoader artifactPluginDescriptorLoader,
                                         DescriptorLoaderRepository descriptorLoaderRepository,
                                         ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder) {
    super(descriptorLoaderRepository, artifactDescriptorValidatorBuilder);

    checkArgument(artifactPluginDescriptorLoader != null, "artifactPluginDescriptorLoader cannot be null");
    this.artifactPluginDescriptorLoader = artifactPluginDescriptorLoader;
  }

  @Override
  protected void doDescriptorConfig(ZernovaPolicyModel artifactModel, PolicyTemplateDescriptor descriptor, File artifactLocation) {
    descriptor.setRootFolder(artifactLocation);
    descriptor.setPlugins(parseArtifactPluginDescriptors(descriptor));
    descriptor.setSupportedJavaVersions(artifactModel.getSupportedJavaVersions());
  }

  private Set<ArtifactPluginDescriptor> parseArtifactPluginDescriptors(PolicyTemplateDescriptor descriptor) {
    Set<BundleDependency> pluginDependencies = descriptor.getClassLoaderConfiguration().getDependencies().stream()
        .filter(dependency -> dependency.getDescriptor().isPlugin()).collect(toSet());

    return pluginDependencies.stream().map(dependency -> {
      try {
        return artifactPluginDescriptorLoader.load(new File(dependency.getBundleUri()), dependency.getDescriptor(), descriptor);
      } catch (IOException e) {
        throw new ZernovaRuntimeException(e);
      }
    }).collect(toSet());
  }

  @Override
  protected AbstractZernovaArtifactModelJsonSerializer<ZernovaPolicyModel> getMuleArtifactModelJsonSerializer() {
    return new ZernovaPolicyModelJsonSerializer();
  }

  @Override
  protected ArtifactType getArtifactType() {
    return POLICY;
  }

  @Override
  protected PolicyTemplateDescriptor createArtifactDescriptor(File artifactLocation, String name,
                                                              Optional<Properties> deploymentProperties) {
    return new PolicyTemplateDescriptor(name, deploymentProperties);
  }
}
