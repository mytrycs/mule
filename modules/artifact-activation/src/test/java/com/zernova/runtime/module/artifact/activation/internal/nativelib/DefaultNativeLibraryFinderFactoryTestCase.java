/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.internal.nativelib;

import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_HOME_DIRECTORY_PROPERTY;
import static com.zernova.tck.ZernovaTestUtils.testWithSystemProperty;

import static java.util.UUID.randomUUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@SmallTest
public class DefaultNativeLibraryFinderFactoryTestCase extends AbstractZernovaTestCase {

  @Rule
  public TemporaryFolder muleHomeFolder = new TemporaryFolder();

  private final DefaultNativeLibraryFinderFactory nativeLibraryFinderFactory = new DefaultNativeLibraryFinderFactory();

  @Test
  public void createsPerAppNativeLibraryFinderWhenPropertyIsFalse() throws Exception {
    doCreateNativeLibraryFinderTest(ArtifactCopyNativeLibraryFinder.class);
  }

  private void doCreateNativeLibraryFinderTest(final Class<? extends NativeLibraryFinder> expectedNativeLibraryFinderClass)
      throws Exception {
    testWithSystemProperty(MULE_HOME_DIRECTORY_PROPERTY, muleHomeFolder.getRoot().getAbsolutePath(),
                           () -> {
                             NativeLibraryFinder nativeLibraryFinder =
                                 nativeLibraryFinderFactory.create("testApp",
                                                                   randomUUID().toString(),
                                                                   new URL[0]);

                             assertThat(nativeLibraryFinder,
                                        instanceOf(expectedNativeLibraryFinderClass));
                           });
  }
}
