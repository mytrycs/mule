/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.internal.maven;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

import com.zernova.maven.client.api.MavenClient;
import com.zernova.maven.client.api.MavenReactorResolver;
import com.zernova.maven.pom.parser.api.model.BundleScope;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDependency;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class DeployableDependencyResolver {

  private static final String MULE_DOMAIN_CLASSIFIER = "mule-domain";

  private final MavenClient muleMavenPluginClient;

  public DeployableDependencyResolver(MavenClient muleMavenPluginClient) {
    this.muleMavenPluginClient = muleMavenPluginClient;
  }

  /**
   * Resolve the deployable dependencies, excluding mule domains.
   *
   * @param pomFile                 pom file
   * @param includeTestDependencies true if the test dependencies must be included, false otherwise.
   * @param mavenReactorResolver    {@link MavenReactorResolver}
   */
  public List<BundleDependency> resolveDeployableDependencies(File pomFile, boolean includeTestDependencies,
                                                              Optional<MavenReactorResolver> mavenReactorResolver) {
    return muleMavenPluginClient
        .resolveArtifactDependencies(pomFile, includeTestDependencies, true, empty(), mavenReactorResolver, empty())
        .stream()
        .filter(d -> !(d.getScope() == BundleScope.PROVIDED)
            || d.getDescriptor().getClassifier().map(MULE_DOMAIN_CLASSIFIER::equals).orElse(false))
        .map(MavenUtilsForArtifact::mavenToArtifact)
        .collect(toList());
  }

}
