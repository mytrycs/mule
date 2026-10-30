/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.connection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.connection.ConnectionHandler;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.tck.testmodels.fruit.Apple;
import com.zernova.tck.testmodels.fruit.Banana;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@SmallTest
public class NullConnectionManagementStrategyTestCase extends AbstractZernovaTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Mock
  private ConnectionProvider<Banana> connectionProvider;

  @Mock
  private Apple config;

  @Mock
  private Banana connection;

  private NullConnectionManagementStrategy<Banana> strategy;

  @Before
  public void before() throws Exception {
    when(connectionProvider.connect()).thenReturn(connection);
    strategy = new NullConnectionManagementStrategy<>(connectionProvider);
  }

  @Test
  public void getConnection() throws Exception {
    ConnectionHandler<Banana> connectionHandler = strategy.getConnectionHandler();
    assertThat(connectionHandler.getConnection(), is(sameInstance(connection)));
  }

  @Test
  public void close() throws Exception {
    strategy.close();
    verify(connectionProvider, never()).disconnect(any(Banana.class));
  }
}
