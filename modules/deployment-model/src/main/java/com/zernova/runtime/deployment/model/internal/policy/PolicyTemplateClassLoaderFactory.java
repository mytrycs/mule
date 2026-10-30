/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.internal.policy;

import com.zernova.runtime.deployment.model.api.policy.PolicyTemplateDescriptor;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import com.zernova.runtime.module.artifact.api.classloader.DeployableArtifactClassLoaderFactory;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaDeployableArtifactClassLoader;

import java.io.File;
import java.util.List;

/**
 * Creates {@link ArtifactClassLoader} for policy templates artifact.
 */
public class PolicyTemplateClassLoaderFactory implements DeployableArtifactClassLoaderFactory<PolicyTemplateDescriptor> {

  @Override
  public ArtifactClassLoader create(String artifactId, ArtifactClassLoader parent, PolicyTemplateDescriptor descriptor) {
    File rootFolder = descriptor.getRootFolder();
    if (rootFolder == null || !rootFolder.exists()) {
      throw new IllegalArgumentException("Policy folder does not exists: " + (rootFolder != null ? rootFolder.getName() : null));
    }

    final ClassLoaderLookupPolicy classLoaderLookupPolicy = parent.getClassLoaderLookupPolicy();

    ZernovaDeployableArtifactClassLoader deployableArtifactClassLoader =
        new ZernovaDeployableArtifactClassLoader(artifactId, descriptor, descriptor.getClassLoaderConfiguration().getUrls(),
                                              parent.getClassLoader(),
                                              classLoaderLookupPolicy);

    return deployableArtifactClassLoader;
  }

  @Override
  public ArtifactClassLoader create(String artifactId, ArtifactClassLoader parent, PolicyTemplateDescriptor descriptor,
                                    List<ArtifactClassLoader> artifactPluginClassLoaders) {
    return create(artifactId, parent, descriptor);
  }
}
