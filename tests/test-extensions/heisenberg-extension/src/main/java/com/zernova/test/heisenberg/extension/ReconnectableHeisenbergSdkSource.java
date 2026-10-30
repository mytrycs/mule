/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.extension.api.annotation.source.EmitsResponse;
import com.zernova.sdk.api.annotation.Alias;
import com.zernova.sdk.api.annotation.Streaming;
import com.zernova.sdk.api.annotation.notification.Fires;
import com.zernova.sdk.api.annotation.param.MediaType;
import com.zernova.sdk.api.annotation.param.Parameter;
import com.zernova.sdk.api.runtime.connectivity.Reconnectable;
import com.zernova.sdk.api.runtime.connectivity.ReconnectionCallback;

@Alias("SdkReconnectableListenPayments")
@EmitsResponse
@Fires(SdkSourceNotificationProvider.class)
@Streaming
@MediaType(TEXT_PLAIN)
public class ReconnectableHeisenbergSdkSource extends SdkHeisenbergSource implements Reconnectable {

  public static int succesfulReconnectionsSdk = 0;
  public static int failedReconnectionsSdk = 0;

  @Parameter
  private boolean reconnectable;

  @Override
  public void reconnect(ConnectionException exception, ReconnectionCallback reconnectionCallback) {
    if (reconnectable) {
      succesfulReconnectionsSdk++;
      reconnectionCallback.success();
    } else {
      failedReconnectionsSdk++;
      reconnectionCallback.failed(new ConnectionException("Boss said no", exception));
    }
  }
}
