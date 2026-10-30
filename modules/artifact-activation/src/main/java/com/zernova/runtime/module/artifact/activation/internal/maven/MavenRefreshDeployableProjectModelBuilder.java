/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.internal.maven;

import com.zernova.maven.client.api.model.MavenConfiguration;
import com.zernova.runtime.api.artifact.ArtifactCoordinates;
import com.zernova.runtime.module.artifact.activation.api.deployable.DeployableProjectModel;
import com.zernova.runtime.module.artifact.activation.api.deployable.ZernovaProjectStructure;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDependency;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MavenRefreshDeployableProjectModelBuilder extends MavenDeployableProjectModelBuilder {

  private final ZernovaProjectStructure projectStructure;
  private final ArtifactCoordinates deployableArtifactCoordinates;

  public MavenRefreshDeployableProjectModelBuilder(ZernovaProjectStructure projectStructure,
                                                   ArtifactCoordinates deployableArtifactCoordinates,
                                                   boolean exportAllResourcesAndPackagesIfEmptyLoaderDescriptor,
                                                   List<BundleDependency> deployableBundleDependencies,
                                                   Set<BundleDescriptor> sharedDeployableBundleDescriptors,
                                                   Map<BundleDescriptor, List<BundleDependency>> additionalPluginDependencies,
                                                   MavenConfiguration mavenConfiguration) {
    super(projectStructure.getProjectFolder().toFile(),
          mavenConfiguration != null ? mavenConfiguration : DEFAULT_MAVEN_CONFIGURATION.get(),
          exportAllResourcesAndPackagesIfEmptyLoaderDescriptor, false);

    this.projectStructure = projectStructure;
    this.deployableArtifactCoordinates = deployableArtifactCoordinates;

    this.deployableBundleDependencies = deployableBundleDependencies;
    this.sharedDeployableBundleDescriptors = sharedDeployableBundleDescriptors;
    this.additionalPluginDependencies = additionalPluginDependencies;
  }

  @Override
  public DeployableProjectModel build() {
    return doBuild(projectStructure, deployableArtifactCoordinates);
  }

}
