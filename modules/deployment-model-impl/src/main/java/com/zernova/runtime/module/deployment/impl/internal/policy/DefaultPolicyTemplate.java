/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.policy;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.runtime.deployment.model.api.plugin.ArtifactPlugin;
import com.zernova.runtime.deployment.model.api.policy.PolicyTemplate;
import com.zernova.runtime.deployment.model.api.policy.PolicyTemplateDescriptor;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaDeployableArtifactClassLoader;

import java.io.File;
import java.util.List;

/**
 * Default implementation of {@link PolicyTemplate}
 */
public class DefaultPolicyTemplate implements PolicyTemplate {

  private final String artifactId;
  private final PolicyTemplateDescriptor descriptor;
  private final ZernovaDeployableArtifactClassLoader policyClassLoader;
  private final List<ArtifactPlugin> artifactPlugins;
  private final List<ArtifactPlugin> ownArtifactPlugins;

  /**
   * Creates a new policy template artifact
   *
   * @param artifactId         artifact unique ID. Non empty.
   * @param descriptor         describes the policy to create. Non null.
   * @param policyClassLoader  classloader to use on this policy. Non null.
   * @param artifactPlugins    artifact plugins deployed only inside the policy. Non null.
   * @param ownArtifactPlugins artifact plugins the policy depends on. Non null.
   */
  public DefaultPolicyTemplate(String artifactId, PolicyTemplateDescriptor descriptor,
                               ZernovaDeployableArtifactClassLoader policyClassLoader,
                               List<ArtifactPlugin> artifactPlugins, List<ArtifactPlugin> ownArtifactPlugins) {
    checkArgument(!isEmpty(artifactId), "artifactId cannot be empty");
    checkArgument(descriptor != null, "descriptor cannot be null");
    checkArgument(policyClassLoader != null, "policyClassLoader cannot be null");
    checkArgument(artifactPlugins != null, "artifactPlugins cannot be null");
    checkArgument(ownArtifactPlugins != null, "ownArtifactPlugin cannot be null");

    this.artifactId = artifactId;
    this.descriptor = descriptor;
    this.policyClassLoader = policyClassLoader;
    this.artifactPlugins = artifactPlugins;
    this.ownArtifactPlugins = ownArtifactPlugins;
  }

  @Override
  public String getArtifactName() {
    return descriptor.getName();
  }

  @Override
  public String getArtifactId() {
    return artifactId;
  }

  @Override
  public PolicyTemplateDescriptor getDescriptor() {
    return descriptor;
  }

  @Override
  public File[] getResourceFiles() {
    return new File[0];
  }

  @Override
  public ArtifactClassLoader getArtifactClassLoader() {
    return policyClassLoader;
  }

  @Override
  public void dispose() {
    if (policyClassLoader != null) {
      policyClassLoader.dispose();
    }
  }

  @Override
  public List<ArtifactPlugin> getArtifactPlugins() {
    return artifactPlugins;
  }

  @Override
  public List<ArtifactPlugin> getOwnArtifactPlugins() {
    return ownArtifactPlugins;
  }
}
