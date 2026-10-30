/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.javaxinject;

import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.sdk.api.annotation.data.sample.SampleData;
import com.zernova.sdk.api.annotation.param.Config;
import com.zernova.sdk.api.annotation.param.Connection;
import com.zernova.sdk.api.annotation.values.OfValues;

import javax.inject.Inject;

public class JavaxInjectCompatibilityTestOperations {

  @Inject
  private ArtifactEncoding encoding;

  @SampleData(JavaxInjectCompatibilityTestSampleDataProvider.class)
  public String execute() {
    return encoding.getDefaultEncoding().name();
  }

  public String executeConnection(@Connection JavaxInjectCompatibilityTestConnection conn) {
    return conn.getEncoding().getDefaultEncoding().name();
  }

  public String executeConfig(@Config JavaxInjectCompatibilityTestConfiguration config) {
    return config.getEncoding().getDefaultEncoding().name();
  }

  public String executePojo(JavaxInjectCompatibilityTestPojo pojoParam) {
    return pojoParam.getEncoding().getDefaultEncoding().name();
  }

  public String valueProvider(@OfValues(JavaxInjectCompatibilityTestValueProvider.class) String param) {
    return param;
  }

}
