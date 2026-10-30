/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.data.sample.extension.provider;

import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.sdk.api.annotation.param.Parameter;
import com.zernova.sdk.api.data.sample.SampleDataException;
import com.zernova.sdk.api.runtime.operation.Result;
import com.zernova.test.data.sample.extension.SampleDataOperations;

import static com.zernova.test.data.sample.extension.SampleDataExtension.adaptLegacy;

public class OptionalTestSampleDataProvider extends TestSampleDataProvider {

  @Parameter
  @Optional
  protected String payload;

  @Parameter
  @Optional(defaultValue = "DEFAULT_VALUE")
  protected String attributes;

  @Override
  public Result<String, String> getSample() throws SampleDataException {
    return adaptLegacy(new SampleDataOperations().connectionLess(payload, attributes));
  }
}
