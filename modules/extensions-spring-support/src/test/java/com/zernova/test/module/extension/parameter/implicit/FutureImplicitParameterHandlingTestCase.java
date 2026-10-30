/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.parameter.implicit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static com.zernova.test.heisenberg.extension.HeisenbergOperations.secretSdkFutureFeature;

import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import org.junit.Test;

public class FutureImplicitParameterHandlingTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "future-implicit-config.xml";
  }

  @Test
  public void sdkFutureImplicitHandling() throws Exception {
    flowRunner("futureSdkImplicitHandling");
    assertThat(secretSdkFutureFeature, is(nullValue()));
  }
}
