/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.clientcredentials;

import static com.zernova.runtime.extension.api.security.CredentialsPlacement.BODY;
import static com.zernova.test.allure.AllureConstants.OauthFeature.SDK_OAUTH_SUPPORT;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.oauth.client.api.ClientCredentialsOAuthDancer;
import com.zernova.oauth.client.api.state.ResourceOwnerOAuthContext;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.retry.ReconnectionConfig;
import com.zernova.runtime.extension.api.connectivity.oauth.ClientCredentialsGrantType;
import com.zernova.runtime.module.extension.internal.util.FieldSetter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;

@Feature(SDK_OAUTH_SUPPORT)
public class ClientCredentialsConnectionProviderWrapperTestCase {

  private ClientCredentialsOAuthHandler oauthHandler;
  private ClientCredentialsGrantType type;
  private ClientCredentialsConnectionProviderWrapper wrapper;

  @Test
  @Issue("W-14391247")
  public void listenerIsUnRegisteredOnStop() throws ZernovaException {
    setupCommonMocks();
    ClientCredentialsOAuthDancer dancer = mock(ClientCredentialsOAuthDancer.class);
    when(oauthHandler.register(any())).thenReturn(dancer);

    wrapper.start();
    wrapper.connect();
    verify(dancer, times(1)).addListener(any());
    wrapper.stop();
    verify(dancer, times(1)).removeListener(any());
  }

  @Test
  public void refreshTokenCallsOauthHandler() throws ZernovaException {
    setupCommonMocks();
    wrapper.refreshToken("id1");
    verify(oauthHandler, times(1)).refreshToken(any());
  }

  @Test
  public void invalidateCallsOauthHandler() throws ZernovaException {
    setupCommonMocks();
    wrapper.invalidate("id1");
    verify(oauthHandler, times(1)).invalidate(any());
  }

  @Test
  public void getOAuthGrantType() throws ZernovaException {
    setupCommonMocks();
    assertThat(wrapper.getGrantType(), is(type));
  }

  private void setupCommonMocks() throws ConnectionException {
    ConnectionProvider delegate = mock(ConnectionProvider.class);
    when(delegate.connect()).thenReturn(new Object());

    ClientCredentialsConfig oauthConfig = mock(ClientCredentialsConfig.class);
    this.type = new ClientCredentialsGrantType("http://accessToken",
                                               "#[accessToken]",
                                               ".*",
                                               null,
                                               BODY);
    when(oauthConfig.getGrantType()).thenReturn(type);
    Map<Field, String> callbackValues = new HashMap<>();
    this.oauthHandler = mock(ClientCredentialsOAuthHandler.class);
    when(oauthHandler.getOAuthContext(any())).thenReturn(mock(ResourceOwnerOAuthContext.class));
    ReconnectionConfig reconnectionConfig = mock(ReconnectionConfig.class);
    this.wrapper =
        new TestClientCredentialsConnectionProviderWrapper(delegate, oauthConfig, callbackValues, oauthHandler,
                                                           reconnectionConfig);
  }

  private class TestClientCredentialsConnectionProviderWrapper extends ClientCredentialsConnectionProviderWrapper {

    public TestClientCredentialsConnectionProviderWrapper(ConnectionProvider delegate, ClientCredentialsConfig oauthConfig,
                                                          Map<Field, String> callbackValues,
                                                          ClientCredentialsOAuthHandler oauthHandler,
                                                          ReconnectionConfig reconnectionConfig) {
      super(delegate, oauthConfig, callbackValues, oauthHandler, reconnectionConfig);
    }

    @Override
    protected FieldSetter<Object, Object> resolveOauthStateSetter(ClientCredentialsConfig oauthConfig) {
      return mock(FieldSetter.class);
    }
  }
}
