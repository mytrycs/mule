/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.api.deployable;

import com.zernova.maven.client.api.model.MavenConfiguration;
import com.zernova.runtime.api.artifact.ArtifactCoordinates;
import com.zernova.runtime.api.deployment.meta.ZernovaDeployableModel;
import com.zernova.runtime.module.artifact.activation.internal.deployable.ZernovaDeployableProjectModelBuilder;
import com.zernova.runtime.module.artifact.activation.internal.maven.MavenDeployableProjectModelBuilder;
import com.zernova.runtime.module.artifact.activation.internal.maven.MavenRefreshDeployableProjectModelBuilder;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDependency;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Generic builder to create a {@link DeployableProjectModel} representing the structure of a project.
 * <p>
 * Implementations may be coupled to a specific build tool, dependency management system or project structure.
 *
 * @since 4.5
 */
public interface DeployableProjectModelBuilder {

  /**
   * @return an implementation of {@link DeployableProjectModelBuilder} that builds a model based on the files provided within a
   *         packaged Mule deployable artifact project.
   *
   * @since 4.8
   */
  static DeployableProjectModelBuilder forMuleProject(File projectFolder, Optional<ZernovaDeployableModel> model) {
    return new ZernovaDeployableProjectModelBuilder(projectFolder, model);
  }

  static DeployableProjectModelBuilder forMavenProject(File projectFolder,
                                                       boolean exportAllResourcesAndPackagesIfEmptyLoaderDescriptor,
                                                       boolean includeTestDependencies) {
    return new MavenDeployableProjectModelBuilder(projectFolder, exportAllResourcesAndPackagesIfEmptyLoaderDescriptor,
                                                  includeTestDependencies);
  }

  static DeployableProjectModelBuilder forMavenProject(File projectFolder,
                                                       MavenConfiguration mavenConfiguration,
                                                       boolean exportAllResourcesAndPackagesIfEmptyLoaderDescriptor,
                                                       boolean includeTestDependencies) {
    if (mavenConfiguration == null) {
      return new MavenDeployableProjectModelBuilder(projectFolder, exportAllResourcesAndPackagesIfEmptyLoaderDescriptor,
                                                    includeTestDependencies);
    } else {
      return new MavenDeployableProjectModelBuilder(projectFolder, mavenConfiguration,
                                                    exportAllResourcesAndPackagesIfEmptyLoaderDescriptor,
                                                    includeTestDependencies);
    }
  }

  static DeployableProjectModelBuilder forMavenRefreshProject(ZernovaProjectStructure projectStructure,
                                                              ArtifactCoordinates deployableArtifactCoordinates,
                                                              boolean exportAllResourcesAndPackagesIfEmptyLoaderDescriptor,
                                                              List<BundleDependency> deployableBundleDependencies,
                                                              Set<BundleDescriptor> sharedDeployableBundleDescriptors,
                                                              Map<BundleDescriptor, List<BundleDependency>> additionalPluginDependencies) {
    return new MavenRefreshDeployableProjectModelBuilder(projectStructure,
                                                         deployableArtifactCoordinates,
                                                         exportAllResourcesAndPackagesIfEmptyLoaderDescriptor,
                                                         deployableBundleDependencies,
                                                         sharedDeployableBundleDescriptors,
                                                         additionalPluginDependencies,
                                                         null);
  }

  static DeployableProjectModelBuilder forMavenRefreshProject(ZernovaProjectStructure projectStructure,
                                                              ArtifactCoordinates deployableArtifactCoordinates,
                                                              boolean exportAllResourcesAndPackagesIfEmptyLoaderDescriptor,
                                                              List<BundleDependency> deployableBundleDependencies,
                                                              Set<BundleDescriptor> sharedDeployableBundleDescriptors,
                                                              Map<BundleDescriptor, List<BundleDependency>> additionalPluginDependencies,
                                                              MavenConfiguration mavenConfiguration) {
    return new MavenRefreshDeployableProjectModelBuilder(projectStructure,
                                                         deployableArtifactCoordinates,
                                                         exportAllResourcesAndPackagesIfEmptyLoaderDescriptor,
                                                         deployableBundleDependencies,
                                                         sharedDeployableBundleDescriptors,
                                                         additionalPluginDependencies,
                                                         mavenConfiguration);
  }

  /**
   * Creates a {@link DeployableProjectModel}.
   *
   * @return a {@link DeployableProjectModel} representing the structure of a project.
   */
  DeployableProjectModel build();

}
