/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal;

import static com.zernova.test.allure.AllureConstants.ClassloadingIsolationFeature.CLASSLOADING_ISOLATION;
import static com.zernova.test.allure.AllureConstants.ClassloadingIsolationFeature.ClassloadingIsolationStory.CLASSLOADER_GENERATION;

import static java.util.Optional.empty;

import static org.apache.commons.io.FileUtils.toFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;

import com.zernova.runtime.deployment.model.api.artifact.ArtifactConfigurationProcessor;
import com.zernova.runtime.deployment.model.api.domain.Domain;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@Feature(CLASSLOADING_ISOLATION)
@Story(CLASSLOADER_GENERATION)
public class ZernovaArtifactResourcesRegistryTestCase extends AbstractZernovaTestCase {

  private static final String DOMAINS_FOLDER = "domains";

  @Test
  @Issue("W-11336719")
  public void containerClassLoaderGeneratedOnlyOnce() throws IOException {
    ZernovaArtifactResourcesRegistry artifactResourcesRegistry = new ZernovaArtifactResourcesRegistry.Builder()
        .artifactConfigurationProcessor(mock(ArtifactConfigurationProcessor.class))
        .build();

    ArtifactClassLoader containerClassLoader = artifactResourcesRegistry.getContainerClassLoader();

    File domainLocation =
        toFile(getClass().getClassLoader().getResource(Paths.get(DOMAINS_FOLDER, "no-dependencies").toString()));
    Domain domain = artifactResourcesRegistry.getDomainFactory().createArtifact(domainLocation, empty());

    ArtifactClassLoader domainClassLoader = domain.getArtifactClassLoader();
    ClassLoader domainRegionClassLoader = domainClassLoader.getClassLoader().getParent();

    assertThat(domainRegionClassLoader.getParent(), sameInstance(containerClassLoader));
  }
}
