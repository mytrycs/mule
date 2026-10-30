/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.support.internal.server;

import static com.zernova.test.allure.AllureConstants.HttpFeature.HTTP_FORWARD_COMPATIBILITY;

import static java.net.InetSocketAddress.createUnresolved;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.http.api.domain.request.ServerConnection;

import java.net.InetSocketAddress;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;

@Feature(HTTP_FORWARD_COMPATIBILITY)
class ServerConnectionWrapperTestCase {

  @Test
  void getLocalHostAddress() {
    InetSocketAddress address = createUnresolved("localhost", 1234);
    ServerConnection muleServerConnection = mock(ServerConnection.class);
    when(muleServerConnection.getLocalHostAddress()).thenReturn(address);
    ServerConnectionWrapper wrapper = new ServerConnectionWrapper(muleServerConnection);
    assertThat(wrapper.getLocalHostAddress(), is(address));
  }
}
