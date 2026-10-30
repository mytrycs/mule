/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.data.sample.extension.source;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.sdk.api.annotation.data.sample.SampleData;
import com.zernova.test.data.sample.extension.SampleDataConnection;
import com.zernova.test.data.sample.extension.provider.ConnectedTestSampleDataProvider;

@Alias("connected-listener")
@SampleData(ConnectedTestSampleDataProvider.class)
@MediaType(TEXT_PLAIN)
public class ConnectedTestSampleDataListener extends SimpleTestSampleDataListener {

  @Connection
  protected ConnectionProvider<SampleDataConnection> connection;

}
