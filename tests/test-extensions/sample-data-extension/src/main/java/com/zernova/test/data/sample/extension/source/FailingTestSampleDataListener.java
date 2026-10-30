/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.data.sample.extension.source;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.annotation.data.sample.SampleData;
import com.zernova.test.data.sample.extension.provider.FailingTestSampleDataProvider;
import com.zernova.test.data.sample.extension.provider.ParameterizedTestSampleDataProvider;

@Alias("failing-listener")
@SampleData(FailingTestSampleDataProvider.class)
@MediaType(TEXT_PLAIN)
public class FailingTestSampleDataListener extends TestSampleDataListener {

  @Parameter
  private String payload;

  @Parameter
  @Optional
  private String attributes;

  @Parameter
  @Optional
  boolean useCustomSampleDataException;

  @Parameter
  @Optional
  boolean withExceptionCause;
}
