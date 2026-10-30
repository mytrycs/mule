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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.tck.testmodels.fruit.Banana;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class PassThroughConnectionHandlerTestCase extends AbstractZernovaTestCase {

  @Mock
  private Banana connection;

  @Mock
  private ConnectionProvider<Banana> connectionProvider;

  private ConnectionHandlerAdapter<Banana> managedConnection;

  @Before
  public void before() {
    managedConnection = new PassThroughConnectionHandler<>(connection, connectionProvider);
  }

  @Test
  public void getConnection() throws Exception {
    assertThat(managedConnection.getConnection(), is(sameInstance(connection)));
  }

  @Test
  public void alwaysReturnsSameConnection() throws Exception {
    getConnection();
    getConnection();
  }

  @Test
  public void release() throws Exception {
    doTwiceDisconnectOnce(managedConnection::release);
  }

  @Test
  public void invalidate() throws Exception {
    doTwiceDisconnectOnce(managedConnection::invalidate);
  }

  @Test
  public void getConnectionProvider() {
    assertThat(managedConnection.getConnectionProvider(), is(sameInstance(connectionProvider)));
  }

  @Test
  public void close() throws Exception {
    doTwiceDisconnectOnce(() -> {
      try {
        managedConnection.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  private void doTwiceDisconnectOnce(Runnable task) {
    task.run();
    task.run();

    verify(connectionProvider).disconnect(connection);
  }
}
