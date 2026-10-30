/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.internal.application;

import static com.zernova.runtime.container.api.ZernovaFoldersUtil.getAppLibFolder;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_HOME_DIRECTORY_PROPERTY;
import static com.zernova.runtime.core.api.util.FileUtils.stringToFile;
import static com.zernova.runtime.deployment.model.api.builder.DeployableArtifactClassLoaderFactoryProvider.applicationClassLoaderFactory;
import static com.zernova.runtime.module.artifact.api.classloader.ParentFirstLookupStrategy.PARENT_FIRST;

import static java.lang.System.setProperty;
import static java.nio.file.Files.createTempDirectory;
import static java.util.Collections.emptyList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.core.api.util.FileUtils;
import com.zernova.runtime.module.artifact.activation.internal.classloader.ZernovaApplicationClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import com.zernova.runtime.module.artifact.api.classloader.DeployableArtifactClassLoaderFactory;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ZernovaApplicationClassLoaderFactoryTestCase extends AbstractZernovaTestCase {

  private static final String DOMAIN_NAME = "test-domain";
  private static final String APP_NAME = "test-app";
  private static final String APP_ID = "test-app-id";

  @Rule
  public TemporaryFolder tempMuleHome = new TemporaryFolder();

  private final ArtifactClassLoader parentArtifactClassLoader = mock(ArtifactClassLoader.class);
  private final ClassLoaderLookupPolicy classLoaderLookupPolicy = mock(ClassLoaderLookupPolicy.class);
  private final DeployableArtifactClassLoaderFactory<ApplicationDescriptor> classLoaderFactory =
      applicationClassLoaderFactory(name -> {
        try {
          return createTempDirectory(name).toFile();
        } catch (IOException e) {
          throw new ZernovaRuntimeException(e);
        }
      });
  private String previousMuleHome;
  private ApplicationDescriptor descriptor;

  @Before
  public void createAppClassLoader() throws IOException {
    // Creates folder structure
    previousMuleHome = setProperty(MULE_HOME_DIRECTORY_PROPERTY, tempMuleHome.getRoot().getAbsolutePath());

    // Add jar file on application's lib folder
    File libDir = getAppLibFolder(APP_NAME);
    assertThat(libDir.mkdirs(), is(true));
    final File appLibrary = new File(libDir, "appLibrary.jar");
    stringToFile(appLibrary.getAbsolutePath(), "Some text");

    when(classLoaderLookupPolicy.getClassLookupStrategy(anyString())).thenReturn(PARENT_FIRST);
    when(parentArtifactClassLoader.getClassLoaderLookupPolicy()).thenReturn(classLoaderLookupPolicy);
    when(parentArtifactClassLoader.getClassLoader()).thenReturn(getClass().getClassLoader());

    when(classLoaderLookupPolicy.extend(anyMap())).thenReturn(classLoaderLookupPolicy);
    descriptor = new ApplicationDescriptor(APP_NAME);
    descriptor.setArtifactLocation(new File(tempMuleHome.newFolder(), APP_NAME));
  }

  @After
  public void cleanUp() {
    if (previousMuleHome != null) {
      setProperty(MULE_HOME_DIRECTORY_PROPERTY, previousMuleHome);
    }
    FileUtils.deleteTree(tempMuleHome.getRoot());
  }

  @Test
  public void createsClassLoader() throws Exception {
    final ZernovaApplicationClassLoader artifactClassLoader =
        (ZernovaApplicationClassLoader) classLoaderFactory.create(APP_ID, parentArtifactClassLoader, descriptor, emptyList());

    assertThat(artifactClassLoader.getParent(), is(parentArtifactClassLoader.getClassLoader()));
    assertThat(artifactClassLoader.getArtifactId(), is(APP_ID));
  }
}
