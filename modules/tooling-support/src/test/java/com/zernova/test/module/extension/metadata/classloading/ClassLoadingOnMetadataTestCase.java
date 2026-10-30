/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.metadata.classloading;

import static com.zernova.runtime.api.component.location.Location.builder;
import static com.zernova.tck.util.TestConnectivityUtils.disableAutomaticTestConnectivity;
import static com.zernova.test.classloading.CLKeysResolver.GET_METADATA;
import static com.zernova.test.classloading.api.ClassLoadingHelper.verifyUsedClassLoaders;

import com.zernova.runtime.api.metadata.MetadataService;
import com.zernova.tck.junit4.rule.SystemProperty;
import com.zernova.test.classloading.api.ClassLoadingHelper;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import jakarta.inject.Inject;

import org.junit.Rule;
import org.junit.Test;

public class ClassLoadingOnMetadataTestCase extends AbstractExtensionFunctionalTestCase {

  @Inject
  private MetadataService metadataService;

  @Rule
  public SystemProperty disableTestConnectivity = disableAutomaticTestConnectivity();

  @Override
  protected void doTearDown() throws Exception {
    ClassLoadingHelper.createdClassLoaders.clear();
  }

  @Override
  protected String getConfigFile() {
    return "classloading/classloading-extension-config.xml";
  }

  @Test
  public void operationWithMetadataResolver() throws Exception {
    metadataService.getMetadataKeys(builder().globalName("none").build());
    verifyUsedClassLoaders(GET_METADATA);
  }

  @Override
  public boolean addToolingObjectsToRegistry() {
    return true;
  }
}
