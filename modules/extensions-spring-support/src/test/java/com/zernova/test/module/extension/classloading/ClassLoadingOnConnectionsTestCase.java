/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.classloading;

import static com.zernova.tck.util.TestConnectivityUtils.disableAutomaticTestConnectivity;
import static com.zernova.test.classloading.CLNoneConnectionProvider.CONNECT;
import static com.zernova.test.classloading.CLNoneConnectionProvider.DISCONNECT;
import static com.zernova.test.classloading.CLPoolingConnectionProvider.ON_BORROW;
import static com.zernova.test.classloading.CLPoolingConnectionProvider.ON_RETURN;
import static com.zernova.test.classloading.api.ClassLoadingHelper.verifyUsedClassLoaders;
import static com.zernova.test.classloading.internal.AllOptionalParameterGroup.ALL_OPTIONAL_PARAMETER_GROUP;

import com.zernova.tck.junit4.rule.SystemProperty;
import com.zernova.test.classloading.api.ClassLoadingHelper;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import org.junit.Rule;
import org.junit.Test;

public class ClassLoadingOnConnectionsTestCase extends AbstractExtensionFunctionalTestCase {

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
  public void noneConnectionProvider() throws Exception {
    flowRunner("none-operation").run();
    verifyUsedClassLoaders(ALL_OPTIONAL_PARAMETER_GROUP, CONNECT, DISCONNECT);
  }

  @Test
  public void cachedConnectionProvider() throws Exception {
    flowRunner("cached-operation").run();
    verifyUsedClassLoaders(ALL_OPTIONAL_PARAMETER_GROUP, CONNECT);
  }

  @Test
  public void poolingConnectionProvider() throws Exception {
    flowRunner("pooling-operation").run();
    verifyUsedClassLoaders(ALL_OPTIONAL_PARAMETER_GROUP, CONNECT, ON_BORROW, ON_RETURN);
  }

  @Test
  public void allOptionalParameterGroup() throws Exception {
    verifyUsedClassLoaders(ALL_OPTIONAL_PARAMETER_GROUP);
  }
}
