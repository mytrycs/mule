/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.artifact;

import com.zernova.runtime.api.deployment.meta.ZernovaApplicationModel;
import com.zernova.runtime.api.deployment.meta.ZernovaPluginModel;
import com.zernova.runtime.api.deployment.meta.ZernovaPolicyModel;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactDescriptorFactoryProvider;
import com.zernova.runtime.deployment.model.api.plugin.resolver.PluginDependenciesResolver;
import com.zernova.runtime.deployment.model.api.policy.PolicyTemplateDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.AbstractArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidatorBuilder;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.DescriptorLoaderRepository;
import com.zernova.runtime.module.deployment.impl.internal.application.ApplicationDescriptorFactory;
import com.zernova.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorFactory;
import com.zernova.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorLoader;
import com.zernova.runtime.module.deployment.impl.internal.plugin.BundlePluginDependenciesResolver;
import com.zernova.runtime.module.deployment.impl.internal.policy.PolicyTemplateDescriptorFactory;

/**
 * Implementation of {@link ArtifactDescriptorFactoryProvider} discoverable through SPI.
 *
 * @since 4.5
 */
public class DefaultArtifactDescriptorFactoryProvider implements ArtifactDescriptorFactoryProvider {

  @Override
  public PluginDependenciesResolver createBundlePluginDependenciesResolver(ArtifactDescriptorFactory<ArtifactPluginDescriptor> artifactPluginDescriptorFactory) {
    return new BundlePluginDependenciesResolver(artifactPluginDescriptorFactory);
  }

  @Override
  public AbstractArtifactDescriptorFactory<ZernovaApplicationModel, ApplicationDescriptor> createApplicationDescriptorFactory(ArtifactDescriptorFactory<ArtifactPluginDescriptor> artifactPluginDescriptorFactory,
                                                                                                                           DescriptorLoaderRepository descriptorLoaderRepository,
                                                                                                                           ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder) {
    return new ApplicationDescriptorFactory(new ArtifactPluginDescriptorLoader(artifactPluginDescriptorFactory),
                                            descriptorLoaderRepository,
                                            artifactDescriptorValidatorBuilder);
  }

  @Override
  public AbstractArtifactDescriptorFactory<ZernovaPluginModel, ArtifactPluginDescriptor> createArtifactPluginDescriptorFactory(DescriptorLoaderRepository descriptorLoaderRepository,
                                                                                                                            ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder) {
    return new ArtifactPluginDescriptorFactory(descriptorLoaderRepository, artifactDescriptorValidatorBuilder);
  }

  @Override
  public AbstractArtifactDescriptorFactory<ZernovaPolicyModel, PolicyTemplateDescriptor> createPolicyTemplateDescriptorFactory() {
    return new PolicyTemplateDescriptorFactory();
  }
}
