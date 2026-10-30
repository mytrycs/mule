/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.oauth;

import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.connectivity.oauth.AuthCodeRequest;
import com.zernova.runtime.extension.api.connectivity.oauth.AuthorizationCodeState;

public class CallbackOperations {

  public void captureCallbackPayloads(@Config AuthCodeConfig config,
                                      @Optional AuthCodeRequest request,
                                      @Optional AuthorizationCodeState state) {
    config.getCapturedAuthCodeRequests().add(request);
    config.getCapturedAuthCodeStates().add(state);
  }

  public void captureSdkCallbackPayloads(@Config AuthCodeConfig config,
                                         @Optional com.zernova.sdk.api.connectivity.oauth.AuthCodeRequest request,
                                         @Optional AuthorizationCodeState state) {
    config.getCapturedAuthCodeRequests().add(request);
    config.getCapturedAuthCodeStates().add(state);
  }
}
