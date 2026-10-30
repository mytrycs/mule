/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.module.extension.internal.ExtensionProperties.CONNECTION_PARAM;
import com.zernova.runtime.api.connection.ConnectionHandler;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class ConnectionArgumentResolverTestCase extends AbstractZernovaTestCase {

  @Mock(answer = RETURNS_DEEP_STUBS)
  private ExecutionContextAdapter operationContext;

  private ConnectionArgumentResolver resolver = new ConnectionArgumentResolver();

  @Test
  public void resolve() throws Exception {
    ConnectionHandler connectionHandler = mock(ConnectionHandler.class);
    final Object connection = new Object();
    when(connectionHandler.getConnection()).thenReturn(connection);
    when(operationContext.getVariable(CONNECTION_PARAM)).thenReturn(connectionHandler);

    assertThat(resolver.resolve(operationContext), is(sameInstance(connection)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void noConnection() {
    when(operationContext.getVariable(CONNECTION_PARAM)).thenReturn(null);
    resolver.resolve(operationContext);
  }
}
