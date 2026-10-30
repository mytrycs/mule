/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.internal.domain;

import static com.zernova.runtime.container.api.ZernovaFoldersUtil.getDomainFolder;
import static com.zernova.runtime.deployment.model.api.domain.DomainDescriptor.DEFAULT_DOMAIN_NAME;

import static java.util.Collections.emptyList;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import com.zernova.runtime.core.api.config.ZernovaProperties;
import com.zernova.runtime.core.api.util.FileUtils;
import com.zernova.runtime.module.artifact.activation.internal.classloader.ZernovaSharedDomainClassLoader;
import com.zernova.runtime.module.artifact.activation.internal.nativelib.ArtifactCopyNativeLibraryFinder;
import com.zernova.runtime.module.artifact.activation.internal.nativelib.NativeLibraryFinder;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.junit4.rule.SystemPropertyTemporaryFolder;
import com.zernova.tck.size.SmallTest;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@SmallTest
public class ZernovaSharedDomainClassLoaderTestCase extends AbstractZernovaTestCase {

  public static final String RESOURCE_FILE_NAME = "file.properties";

  private static final String CUSTOM_DOMAIN_NAME = "custom-domain";
  private static final String NATIVE_LIBRARY = "native-library";

  @Rule
  public SystemPropertyTemporaryFolder temporaryFolder =
      new SystemPropertyTemporaryFolder(ZernovaProperties.MULE_HOME_DIRECTORY_PROPERTY);
  private final ClassLoaderLookupPolicy lookupPolicy = mock(ClassLoaderLookupPolicy.class);

  @Rule
  public TemporaryFolder nativeLibraryFolder = new TemporaryFolder();

  @Test
  public void findResourcesInProvidedUrls() throws Exception {
    createDomainFolder(DEFAULT_DOMAIN_NAME);
    final File resourceFile = createDomainResource(DEFAULT_DOMAIN_NAME, RESOURCE_FILE_NAME);
    final List<URL> urls = Collections.singletonList(resourceFile.toURI().toURL());

    ZernovaSharedDomainClassLoader classLoader = new ZernovaSharedDomainClassLoader(new ArtifactDescriptor(DEFAULT_DOMAIN_NAME),
                                                                              getClass().getClassLoader(), lookupPolicy, urls);

    assertThat(classLoader.findResource(RESOURCE_FILE_NAME), notNullValue());
  }

  private File createDomainResource(String domainName, String resourceFile) throws Exception {
    final File file = new File(getDomainFolder(domainName), resourceFile);
    assertThat(FileUtils.createFile(file.getAbsolutePath()).exists(), is(true));

    return file;
  }

  private void createDomainFolder(String domainName) {
    assertThat(getDomainFolder(domainName).mkdirs(), is(true));
  }

  @Test
  public void defaultDomainNotUseNativeLibrary() {
    ZernovaSharedDomainClassLoader classLoader = new ZernovaSharedDomainClassLoader(new ArtifactDescriptor(DEFAULT_DOMAIN_NAME),
                                                                              getClass().getClassLoader(), lookupPolicy,
                                                                              emptyList());

    assertThat(classLoader.findLibrary(NATIVE_LIBRARY), is(nullValue()));
  }

  @Test
  public void customDomainLoadNativeLibrary() throws Exception {
    // Create native library
    File nativeLibrary = createNativeLibraryFile(NATIVE_LIBRARY);
    NativeLibraryFinder nativeLibraryFinder =
        new ArtifactCopyNativeLibraryFinder(nativeLibraryFolder.getRoot(), new URL[] {nativeLibrary.toURL()});

    ZernovaSharedDomainClassLoader classLoader = new ZernovaSharedDomainClassLoader(new ArtifactDescriptor(CUSTOM_DOMAIN_NAME),
                                                                              getClass().getClassLoader(), lookupPolicy,
                                                                              emptyList(), nativeLibraryFinder);

    assertThat(classLoader.findLibrary(NATIVE_LIBRARY), is(notNullValue()));
  }

  private File createNativeLibraryFile(String libName) throws Exception {
    return createNativeLibraryFile(nativeLibraryFolder.getRoot(), System.mapLibraryName(libName));
  }

  private File createNativeLibraryFile(File folder, String libFileName) throws Exception {
    File libraryFile = new File(folder, libFileName);
    assertThat(FileUtils.createFile(libraryFile.getAbsolutePath()).exists(), is(true));
    return libraryFile;
  }
}
