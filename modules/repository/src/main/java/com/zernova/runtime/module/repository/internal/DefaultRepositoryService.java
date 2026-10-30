/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.repository.internal;

import static com.zernova.runtime.module.repository.internal.RepositoryServiceFactory.MULE_REMOTE_REPOSITORIES_PROPERTY;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.maven.client.api.BundleDependenciesResolutionException;
import com.zernova.maven.client.api.MavenClient;
import com.zernova.maven.client.api.exception.BundleDependencyNotFoundException;
import com.zernova.maven.pom.parser.api.model.BundleDescriptor;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDependency;
import com.zernova.runtime.module.repository.api.BundleNotFoundException;
import com.zernova.runtime.module.repository.api.RepositoryConnectionException;
import com.zernova.runtime.module.repository.api.RepositoryService;
import com.zernova.runtime.module.repository.api.RepositoryServiceDisabledException;

import java.io.File;

import org.slf4j.Logger;

/**
 * Default implementation for {@code RepositoryService}.
 *
 * @since 4.0
 */
public class DefaultRepositoryService implements RepositoryService, Disposable {

  private static final Logger LOGGER = getLogger(DefaultRepositoryService.class);

  private final MavenClient mavenClient;

  public DefaultRepositoryService(MavenClient mavenClient) {
    this.mavenClient = mavenClient;
  }

  @Override
  public File lookupBundle(BundleDependency bundleDependency) {
    try {
      if (mavenClient.getMavenConfiguration().getMavenRemoteRepositories().isEmpty()) {
        throw new RepositoryServiceDisabledException("Repository service has not been configured so it's disabled. "
            + "To enable it you must configure the set of repositories to use using the system property: "
            + MULE_REMOTE_REPOSITORIES_PROPERTY);
      }

      com.zernova.maven.pom.parser.api.model.BundleDependency resolvedBundleDependency =
          mavenClient.resolveBundleDescriptor(muleToMavenDescriptor(bundleDependency.getDescriptor()));

      return new File(resolvedBundleDependency.getBundleUri().getPath());
    } catch (BundleDependencyNotFoundException e) {
      throw new BundleNotFoundException(e);
    } catch (BundleDependenciesResolutionException e) {
      throw new RepositoryConnectionException("There was a problem connecting to one of the repositories", e);
    }
  }

  private BundleDescriptor muleToMavenDescriptor(com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor bundleDescriptor) {
    return new com.zernova.maven.pom.parser.api.model.BundleDescriptor.Builder()
        .setGroupId(bundleDescriptor.getGroupId())
        .setArtifactId(bundleDescriptor.getArtifactId())
        .setVersion(bundleDescriptor.getVersion())
        .setBaseVersion(bundleDescriptor.getBaseVersion() != null ? bundleDescriptor.getBaseVersion()
            : bundleDescriptor.getVersion())
        .setType(bundleDescriptor.getType())
        .setClassifier(bundleDescriptor.getClassifier().orElse(null))
        .build();
  }

  @Override
  public void dispose() {
    try {
      mavenClient.close();
    } catch (Exception e) {
      LOGGER.error("Error while disposing 'mavenClient'", e);
    }
  }

}
