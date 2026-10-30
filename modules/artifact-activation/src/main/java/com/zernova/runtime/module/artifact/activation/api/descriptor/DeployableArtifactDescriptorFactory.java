/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.api.descriptor;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.deployment.meta.ZernovaPluginModel;
import com.zernova.runtime.module.artifact.activation.api.deployable.DeployableProjectModel;
import com.zernova.runtime.module.artifact.activation.api.plugin.PluginDescriptorResolver;
import com.zernova.runtime.module.artifact.activation.api.plugin.PluginModelResolver;
import com.zernova.runtime.module.artifact.activation.internal.descriptor.DefaultDeployableArtifactDescriptorFactory;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor;

import java.util.Map;

/**
 * Provides methods to create the descriptors of deployable artifacts (i.e. applications and domains with the descriptors for
 * their plugins within) from a {@link DeployableProjectModel}.
 *
 * @since 4.5
 */
@NoImplement
public interface DeployableArtifactDescriptorFactory {

  static DeployableArtifactDescriptorFactory defaultArtifactDescriptorFactory() {
    return new DefaultDeployableArtifactDescriptorFactory();
  }

  /**
   * Creates a descriptor for a domain, including its plugin descriptors.
   *
   * @param model                    model describing the structure of the domain with all the necessary information to build its
   *                                 descriptor.
   * @param deploymentProperties     properties that affect how the artifact is deployed.
   * @param pluginModelResolver      resolves {@link ZernovaPluginModel} from a dependency. Default implementation is
   *                                 {@link PluginModelResolver#pluginModelResolver()}.
   * @param pluginDescriptorResolver a wrapper function around the logic to extract an {@link ArtifactPluginDescriptor} from the
   *                                 jar described by the {@link BundleDescriptor}, otherwise it will be created.
   * @param descriptorCreator        creates the descriptor's instance that will be populated by the factory.
   * @return a descriptor for a domain.
   */
  DomainDescriptor createDomainDescriptor(DeployableProjectModel model,
                                          Map<String, String> deploymentProperties,
                                          PluginModelResolver pluginModelResolver,
                                          PluginDescriptorResolver pluginDescriptorResolver,
                                          DeployableArtifactDescriptorCreator<DomainDescriptor> descriptorCreator);

  /**
   * Creates a descriptor for a domain, including its plugin descriptors, using the default resolvers.
   *
   * @param model                model describing the structure of the domain with all the necessary information to build its
   *                             descriptor.
   * @param deploymentProperties properties that affect how the artifact is deployed.
   * @param descriptorCreator    creates the descriptor's instance that will be populated by the factory.
   * @return a descriptor for a domain.
   */
  DomainDescriptor createDomainDescriptor(DeployableProjectModel model,
                                          Map<String, String> deploymentProperties,
                                          DeployableArtifactDescriptorCreator<DomainDescriptor> descriptorCreator);

  /**
   * Creates a descriptor for a domain, including its plugin descriptors, using the default resolvers.
   *
   * @param model                model describing the structure of the domain with all the necessary information to build its
   *                             descriptor.
   * @param deploymentProperties properties that affect how the artifact is deployed.
   * @return a descriptor for a domain.
   */
  DomainDescriptor createDomainDescriptor(DeployableProjectModel model,
                                          Map<String, String> deploymentProperties);

  /**
   * Creates a descriptor for an application, including its plugin descriptors.
   *
   * @param model                    model describing the structure of the application with all the necessary information to build
   *                                 its descriptor.
   * @param deploymentProperties     properties that affect how the artifact is deployed.
   * @param pluginModelResolver      resolves {@link ZernovaPluginModel} from a dependency. Default implementation is
   *                                 {@link PluginModelResolver#pluginModelResolver()}.
   * @param pluginDescriptorResolver a wrapper function around the logic to extract an {@link ArtifactPluginDescriptor} from the
   *                                 jar described by the {@link BundleDescriptor}, otherwise it will be created.
   * @param domainDescriptorResolver a wrapper function around the logic to obtain a {@link DomainDescriptor}.
   * @param descriptorCreator        creates the descriptor's instance that will be populated by the factory.
   * @return a descriptor for an application.
   */
  ApplicationDescriptor createApplicationDescriptor(DeployableProjectModel model,
                                                    Map<String, String> deploymentProperties,
                                                    PluginModelResolver pluginModelResolver,
                                                    PluginDescriptorResolver pluginDescriptorResolver,
                                                    DomainDescriptorResolver domainDescriptorResolver,
                                                    DeployableArtifactDescriptorCreator<ApplicationDescriptor> descriptorCreator);

  /**
   * Creates a descriptor for an application, including its plugin descriptors.
   *
   * @param model                    model describing the structure of the application with all the necessary information to build
   *                                 its descriptor.
   * @param deploymentProperties     properties that affect how the artifact is deployed.
   * @param pluginModelResolver      resolves {@link ZernovaPluginModel} from a dependency. Default implementation is
   *                                 {@link PluginModelResolver#pluginModelResolver()}.
   * @param pluginDescriptorResolver a wrapper function around the logic to extract an {@link ArtifactPluginDescriptor} from the
   *                                 jar described by the {@link BundleDescriptor}, otherwise it will be created.
   * @return a descriptor for an application.
   */
  ApplicationDescriptor createApplicationDescriptor(DeployableProjectModel model,
                                                    Map<String, String> deploymentProperties,
                                                    PluginModelResolver pluginModelResolver,
                                                    PluginDescriptorResolver pluginDescriptorResolver);

  /**
   * Creates a descriptor for an application, including its plugin descriptors, using the default resolvers.
   *
   * @param model                    model describing the structure of the application with all the necessary information to build
   *                                 its descriptor.
   * @param deploymentProperties     properties that affect how the artifact is deployed.
   * @param domainDescriptorResolver a wrapper function around the logic to obtain a {@link DomainDescriptor}.
   * @return a descriptor for an application.
   */
  ApplicationDescriptor createApplicationDescriptor(DeployableProjectModel model,
                                                    Map<String, String> deploymentProperties,
                                                    DomainDescriptorResolver domainDescriptorResolver);

  /**
   * Creates a descriptor for an application, including its plugin descriptors, using the default resolvers.
   *
   * @param model                    model describing the structure of the application with all the necessary information to build
   *                                 its descriptor.
   * @param deploymentProperties     properties that affect how the artifact is deployed.
   * @param domainDescriptorResolver a wrapper function around the logic to obtain a {@link DomainDescriptor}.
   * @param descriptorCreator        creates the descriptor's instance that will be populated by the factory.
   * @return a descriptor for an application.
   */
  ApplicationDescriptor createApplicationDescriptor(DeployableProjectModel model,
                                                    Map<String, String> deploymentProperties,
                                                    DomainDescriptorResolver domainDescriptorResolver,
                                                    DeployableArtifactDescriptorCreator<ApplicationDescriptor> descriptorCreator);

  /**
   * Creates a descriptor for an application, including its plugin descriptors, using the default resolvers.
   *
   * @param model                model describing the structure of the application with all the necessary information to build its
   *                             descriptor.
   * @param deploymentProperties properties that affect how the artifact is deployed.
   * @return a descriptor for an application.
   */
  ApplicationDescriptor createApplicationDescriptor(DeployableProjectModel model,
                                                    Map<String, String> deploymentProperties);

}
