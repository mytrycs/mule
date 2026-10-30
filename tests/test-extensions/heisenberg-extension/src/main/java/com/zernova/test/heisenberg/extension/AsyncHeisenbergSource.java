/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import static com.zernova.runtime.extension.api.annotation.param.Optional.PAYLOAD;
import static com.zernova.test.heisenberg.extension.HeisenbergExtension.RICIN_GROUP_NAME;

import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.Streaming;
import com.zernova.runtime.extension.api.annotation.execution.OnError;
import com.zernova.runtime.extension.api.annotation.execution.OnSuccess;
import com.zernova.runtime.extension.api.annotation.notification.Fires;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.annotation.param.stereotype.Stereotype;
import com.zernova.sdk.api.annotation.source.EmitsResponse;
import com.zernova.runtime.extension.api.notification.NotificationEmitter;
import com.zernova.runtime.extension.api.runtime.source.SourceCompletionCallback;
import com.zernova.test.heisenberg.extension.model.Methylamine;
import com.zernova.test.heisenberg.extension.model.PersonalInfo;
import com.zernova.test.heisenberg.extension.stereotypes.AsyncSourceStereotype;

@Alias("AsyncListenPayments")
@EmitsResponse
@Fires(SourceNotificationProvider.class)
@Streaming
@Stereotype(AsyncSourceStereotype.class)
@MediaType(TEXT_PLAIN)
public class AsyncHeisenbergSource extends HeisenbergSource {

  public static SourceCompletionCallback completionCallback;
  public static com.zernova.sdk.api.runtime.source.SourceCompletionCallback sdkCompletionCallback;

  @OnSuccess
  public void onSuccess(@Optional(defaultValue = PAYLOAD) Long payment, @Optional String sameNameParameter,
                        @com.zernova.sdk.api.annotation.param.ParameterGroup(name = RICIN_GROUP_NAME) RicinGroup ricin,
                        @ParameterGroup(name = "Success Info", showInDsl = true) PersonalInfo successInfo,
                        @Optional boolean fail,
                        com.zernova.sdk.api.runtime.source.SourceCompletionCallback sdkCompletionCallback,
                        NotificationEmitter notificationEmitter) {

    AsyncHeisenbergSource.sdkCompletionCallback = sdkCompletionCallback;

    try {
      super.onSuccess(payment, sameNameParameter, ricin, successInfo, fail, notificationEmitter);
      sdkCompletionCallback.success();
    } catch (Throwable t) {
      sdkCompletionCallback.error(t);
    }
  }

  @OnError
  public void onError(Error error, @Optional String sameNameParameter, @Optional Methylamine methylamine,
                      @ParameterGroup(name = RICIN_GROUP_NAME) RicinGroup ricin,
                      @com.zernova.sdk.api.annotation.param.ParameterGroup(name = "Error Info",
                          showInDsl = true) PersonalInfo infoError,
                      @Optional boolean propagateError,
                      SourceCompletionCallback completionCallback,
                      NotificationEmitter notificationEmitter) {

    AsyncHeisenbergSource.completionCallback = completionCallback;

    try {
      super.onError(error, sameNameParameter, methylamine, ricin, infoError, propagateError, notificationEmitter);
      completionCallback.success();
    } catch (Throwable t) {
      completionCallback.error(t);
    }
  }
}
