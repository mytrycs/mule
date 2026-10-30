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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.connection.ConnectionHandler;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.util.concurrent.Latch;
import com.zernova.runtime.core.api.lifecycle.LifecycleState;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.tck.testmodels.fruit.Banana;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@SmallTest
public class CachedConnectionManagementStrategyTestCase extends AbstractZernovaTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  private Banana connection = new Banana();

  @Mock
  private ConnectionProvider<Banana> connectionProvider;

  @Mock
  private LifecycleState deploymentLifecycleState;

  private CachedConnectionManagementStrategy<Banana> connectionStrategy;

  @Before
  public void before() throws Exception {
    when(connectionProvider.connect()).thenReturn(connection);
    connectionStrategy = new CachedConnectionManagementStrategy<>(connectionProvider, deploymentLifecycleState);
  }

  @Test
  public void getConnection() throws Exception {
    ConnectionHandler<Banana> connectionHandler = connectionStrategy.getConnectionHandler();

    Banana connection = connectionHandler.getConnection();
    verify(connectionProvider).connect();
    assertThat(connection, is(sameInstance(this.connection)));
  }

  @Test
  public void getConnectionIsIdempotent() throws Exception {
    getConnection();

    reset(connectionProvider);

    Banana connection = connectionStrategy.getConnectionHandler().getConnection();
    verify(connectionProvider, never()).connect();
    assertThat(connection, is(sameInstance(this.connection)));
  }

  @Test
  public void close() throws Exception {
    connectionStrategy.getConnectionHandler().getConnection();
    connectionStrategy.close();
    verify(connectionProvider).disconnect(connection);
  }

  @Test
  public void getConnectionConcurrentlyAndNeverConnect() throws Exception {
    Latch latch = new Latch();
    when(connectionProvider.connect()).thenAnswer(invocation -> {
      new Thread(() -> {
        try {
          latch.release();
          getConnection();
        } catch (Exception e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException(e);
        }
      }).start();

      return connection;
    });

    Banana connection = connectionStrategy.getConnectionHandler().getConnection();
    assertThat(latch.await(5, TimeUnit.SECONDS), is(true));
    assertThat(connection, is(sameInstance(this.connection)));
    verify(connectionProvider).connect();
  }
}
