/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.service.internal.test.artifact;

import static com.zernova.runtime.api.artifact.ArtifactType.SERVICE;
import static com.zernova.runtime.module.artifact.api.classloader.ParentFirstLookupStrategy.PARENT_FIRST;
import static com.zernova.runtime.module.service.api.artifact.ServiceClassLoaderFactoryProvider.serviceClassLoaderConfigurationLoader;
import static com.zernova.runtime.module.service.internal.artifact.LibFolderClassLoaderConfigurationLoader.LIB_FOLDER;
import static com.zernova.test.allure.AllureConstants.ClassloadingIsolationFeature.CLASSLOADING_ISOLATION;
import static com.zernova.test.allure.AllureConstants.ClassloadingIsolationFeature.ClassloadingIsolationStory.CLASSLOADER_CONFIGURATION;
import static com.zernova.test.allure.AllureConstants.ClassloadingIsolationFeature.ClassloadingIsolationStory.CLASSLOADER_CONFIGURATION_LOADER;

import static java.util.Collections.emptyMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.ArrayMatching.hasItemInArray;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import com.zernova.runtime.module.artifact.api.descriptor.ClassLoaderConfiguration;
import com.zernova.runtime.module.artifact.api.descriptor.ClassLoaderConfigurationLoader;
import com.zernova.runtime.module.service.api.artifact.ServiceDescriptor;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.qameta.allure.Feature;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;

@Feature(CLASSLOADING_ISOLATION)
@Stories({@Story(CLASSLOADER_CONFIGURATION_LOADER), @Story(CLASSLOADER_CONFIGURATION)})
public class LibFolderClassLoaderConfigurationLoaderTestCase extends AbstractZernovaTestCase {

  private final ClassLoaderLookupPolicy lookupPolicy = mock(ClassLoaderLookupPolicy.class);

  @Rule
  public TemporaryFolder serviceFolder = new TemporaryFolder();
  private final ClassLoaderConfigurationLoader classLoaderConfigurationLoader = serviceClassLoaderConfigurationLoader();
  private ServiceDescriptor descriptor;

  @Before
  public void setUp() throws Exception {
    descriptor = new ServiceDescriptor("testService");
    descriptor.setRootFolder(serviceFolder.getRoot());

    when(lookupPolicy.getClassLookupStrategy(anyString())).thenReturn(PARENT_FIRST);
  }

  @Test(expected = IllegalArgumentException.class)
  public void validatesServiceFolder() throws Exception {
    File fakeServiceFolder = new File("./fake/folder/for/test");
    descriptor.setRootFolder(fakeServiceFolder);
    classLoaderConfigurationLoader.load(fakeServiceFolder, emptyMap(), SERVICE);
  }

  @Test
  public void addsArtifactFolderToClassLoader() throws Exception {
    ClassLoaderConfiguration classLoaderModel = classLoaderConfigurationLoader.load(serviceFolder.getRoot(), emptyMap(), SERVICE);

    URL[] urls = classLoaderModel.getUrls();
    assertThat(urls, arrayWithSize(1));
    assertThat(urls, hasItemInArray(serviceFolder.getRoot().toURI().toURL()));
  }

  @Test
  public void addJarsFromLibFolderToClassLoader() throws Exception {
    File libFolder = serviceFolder.newFolder(LIB_FOLDER);
    File jarFile = new File(libFolder, "dummy.jar");
    jarFile.createNewFile();


    ClassLoaderConfiguration classLoaderModel = classLoaderConfigurationLoader.load(serviceFolder.getRoot(), emptyMap(), SERVICE);

    URL[] urls = classLoaderModel.getUrls();
    assertThat(urls, arrayWithSize(2));
    assertThat(urls, hasItemInArray(jarFile.toURI().toURL()));
  }

  @Test
  public void ignoresNonJarsFilesFromLibFolder() throws Exception {
    File libFolder = serviceFolder.newFolder(LIB_FOLDER);
    File jarFile = new File(libFolder, "dummy.txt");
    jarFile.createNewFile();

    ClassLoaderConfiguration classLoaderModel = classLoaderConfigurationLoader.load(serviceFolder.getRoot(), emptyMap(), SERVICE);

    // Contains only the service root
    assertThat(classLoaderModel.getUrls(), hasItemInArray(serviceFolder.getRoot().toURI().toURL()));
  }
}
