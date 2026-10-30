/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.support.internal.client;

import static com.zernova.test.allure.AllureConstants.HttpFeature.HTTP_FORWARD_COMPATIBILITY;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.http.api.sse.ServerSentEvent;
import com.zernova.sdk.api.http.sse.client.SseListener;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Feature(HTTP_FORWARD_COMPATIBILITY)
@ExtendWith(MockitoExtension.class)
class SseListenerWrapperTestCase {

  @Mock
  private SseListener listenerWrapper;

  @Mock
  private ServerSentEvent muleSseEvent;

  private SseListenerWrapper sseEventWrapper;

  @BeforeEach
  void setUp() {
    sseEventWrapper = new SseListenerWrapper(listenerWrapper);
  }

  @Test
  void delegateOnEvent() {
    String theData = "The Capiangos were the Federal wolf-men";
    when(muleSseEvent.getData()).thenReturn(theData);
    sseEventWrapper.onEvent(muleSseEvent);

    var eventWrapperCaptor = forClass(com.zernova.sdk.api.http.sse.ServerSentEvent.class);
    verify(listenerWrapper).onEvent(eventWrapperCaptor.capture());

    var eventWrapper = eventWrapperCaptor.getValue();
    assertThat(eventWrapper.getData(), is(theData));
  }

  @Test
  void delegateOnClose() {
    sseEventWrapper.onClose();
    verify(listenerWrapper).onClose();
  }
}
