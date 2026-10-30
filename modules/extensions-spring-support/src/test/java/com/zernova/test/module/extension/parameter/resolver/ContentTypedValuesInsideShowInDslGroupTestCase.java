/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.parameter.resolver;

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
public class ContentTypedValuesInsideShowInDslGroupTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "parameter/content-typed-values-inside-show-in-dsl-group-resolver.xml";
  }

  @Test
  @Issue("MULE-19616")
  public void contentTypedValuesInsideShowInDslGroup() throws Exception {
    CoreEvent result = flowRunner("contentTypedValuesInsideShowInDslGroup").withPayload("abc").run();
    assertThat(result.getMessage().getPayload().getValue(), is("123"));
  }

}
