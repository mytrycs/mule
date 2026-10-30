/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.support.internal.server;

import static com.zernova.test.allure.AllureConstants.HttpFeature.HTTP_FORWARD_COMPATIBILITY;

import static org.mockito.Mockito.verify;

import com.zernova.runtime.http.api.server.RequestHandlerManager;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Feature(HTTP_FORWARD_COMPATIBILITY)
@ExtendWith(MockitoExtension.class)
class EndpointAvailabilityHandlerWrapperTestCase {

  @Mock
  private RequestHandlerManager mockHandler;

  private EndpointAvailabilityHandlerWrapper handlerWrapper;

  @BeforeEach
  void setUp() {
    handlerWrapper = new EndpointAvailabilityHandlerWrapper(mockHandler);
  }

  @Test
  void availableCallsStart() {
    handlerWrapper.available();
    verify(mockHandler).start();
  }

  @Test
  void unavailableCallsStop() {
    handlerWrapper.unavailable();
    verify(mockHandler).stop();
  }

  @Test
  void removeCallsDispose() {
    handlerWrapper.remove();
    verify(mockHandler).dispose();
  }
}
