/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.internal.plugin;

import com.zernova.runtime.api.deployment.meta.ZernovaArtifactLoaderDescriptor;
import com.zernova.runtime.api.deployment.meta.ZernovaPluginModel;
import com.zernova.runtime.module.artifact.activation.internal.descriptor.AbstractArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidatorBuilder;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDependency;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ClassLoaderConfiguration;
import com.zernova.runtime.module.artifact.api.descriptor.DeployableArtifactDescriptor;
import com.zernova.runtime.module.artifact.api.plugin.LoaderDescriber;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Creates an artifact descriptor for a plugin.
 */
public class ArtifactPluginDescriptorFactory
    extends AbstractArtifactDescriptorFactory<ZernovaPluginModel, ArtifactPluginDescriptor> {

  private final ZernovaPluginModel pluginModel;
  private final BundleDependency bundleDependency;
  private final DeployableArtifactDescriptor ownerDescriptor;
  private final List<BundleDependency> bundleDependencies;
  private final Set<BundleDescriptor> sharedProjectDependencies;

  public ArtifactPluginDescriptorFactory(BundleDependency bundleDependency,
                                         ZernovaPluginModel pluginModel,
                                         DeployableArtifactDescriptor ownerDescriptor,
                                         List<BundleDependency> bundleDependencies,
                                         Set<BundleDescriptor> sharedProjectDependencies,
                                         ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder) {
    super(new File(bundleDependency.getBundleUri()), artifactDescriptorValidatorBuilder);

    this.pluginModel = pluginModel;
    this.bundleDependency = bundleDependency;
    this.ownerDescriptor = ownerDescriptor;
    this.bundleDependencies = bundleDependencies;
    this.sharedProjectDependencies = sharedProjectDependencies;
  }

  @Override
  protected ZernovaPluginModel createArtifactModel() {
    return pluginModel;
  }

  @Override
  protected void doDescriptorConfig(ArtifactPluginDescriptor descriptor) {
    getArtifactModel().getExtensionModelLoaderDescriptor().ifPresent(extensionModelDescriptor -> {
      final LoaderDescriber loaderDescriber = new LoaderDescriber(extensionModelDescriptor.getId());
      loaderDescriber.addAttributes(extensionModelDescriptor.getAttributes());
      descriptor.setExtensionModelDescriptorProperty(loaderDescriber);
    });

    getArtifactModel().getLicense().ifPresent(descriptor::setLicenseModel);
  }

  @Override
  protected ClassLoaderConfiguration getClassLoaderConfiguration(ZernovaArtifactLoaderDescriptor muleArtifactLoaderDescriptor) {
    return new PluginClassLoaderConfigurationAssembler(bundleDependency,
                                                       sharedProjectDependencies,
                                                       getArtifactLocation(),
                                                       muleArtifactLoaderDescriptor,
                                                       bundleDependencies,
                                                       ownerDescriptor)
                                                           .createClassLoaderConfiguration();
  }

  @Override
  protected BundleDescriptor getBundleDescriptor() {
    return bundleDependency.getDescriptor();
  }

  @Override
  protected ArtifactPluginDescriptor doCreateArtifactDescriptor() {
    return new ArtifactPluginDescriptor(getArtifactModel().getName());
  }
}
