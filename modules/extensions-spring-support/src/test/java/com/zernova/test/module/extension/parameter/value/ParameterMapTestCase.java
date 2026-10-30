/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.parameter.value;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static com.zernova.test.allure.AllureConstants.JavaSdk.JAVA_SDK;
import static com.zernova.test.allure.AllureConstants.JavaSdk.Parameters.PARAMETERS;

import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@Feature(JAVA_SDK)
@Story(PARAMETERS)
public class ParameterMapTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "parameter/parameter-map-config.xml";
  }

  @Test
  @Issue("MULE-19606")
  public void operationWithScalarsMapParam() throws Exception {
    CoreEvent result = flowRunner("operationWithScalarsMapParam").withPayload("expr").run();
    assertThat(result.getMessage().getPayload().getValue(), is("{num=1, str=one, expr=expr}"));
  }
}
