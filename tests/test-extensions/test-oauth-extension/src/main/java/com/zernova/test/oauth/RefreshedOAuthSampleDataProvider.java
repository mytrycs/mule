/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.oauth;

import static com.zernova.sdk.api.runtime.operation.Result.builder;

import com.zernova.sdk.api.annotation.param.Connection;
import com.zernova.sdk.api.connectivity.oauth.AccessTokenExpiredException;
import com.zernova.sdk.api.data.sample.SampleDataException;
import com.zernova.sdk.api.data.sample.SampleDataProvider;
import com.zernova.sdk.api.runtime.operation.Result;

public class RefreshedOAuthSampleDataProvider implements SampleDataProvider<String, String> {

  public static final String SAMPLE_PAYLOAD_VALUE = "Sample payload!";
  public static final String SAMPLE_ATTRIBUTES_VALUE = "Sample Attributes!";

  @Connection
  private TestOAuthConnection testOAuthConnection;

  @Override
  public String getId() {
    return "OAuth sample data";
  }

  @Override
  public Result getSample() throws SampleDataException {
    if (!testOAuthConnection.getState().getState().getAccessToken().contains("refresh")) {
      throw new AccessTokenExpiredException();
    }
    return builder()
        .output(SAMPLE_PAYLOAD_VALUE)
        .attributes(SAMPLE_ATTRIBUTES_VALUE)
        .build();
  }
}
