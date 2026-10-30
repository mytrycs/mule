/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source;

import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;

import com.zernova.runtime.api.component.execution.CompletableCallback;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.sdk.api.runtime.source.SourceCallbackContext;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ComposedSourceCallbackExecutorTestCase extends AbstractZernovaTestCase {

  @Mock
  private SourceCallbackExecutor first;

  @Mock
  private SourceCallbackExecutor then;

  @Mock
  private CoreEvent event;

  @Mock
  private SourceCallbackContext context;

  @Mock
  private CompletableCallback<Void> callback;

  private Map parameters = emptyMap();



  private ComposedSourceCallbackExecutor executor;

  @Before
  public void before() {
    executor = new ComposedSourceCallbackExecutor(first, then);
  }

  @Test
  public void bothSuccessful() {
    mockSuccessful(first);
    mockSuccessful(then);

    executor.execute(event, parameters, context, callback);

    InOrder inOrder = inOrder(first, then, callback);
    inOrder.verify(first).execute(same(event), same(parameters), same(context), any());
    inOrder.verify(then).execute(same(event), same(parameters), same(context), any());
    inOrder.verify(callback).complete(null);
  }

  @Test
  public void firstExecutorFails() {
    final Exception e = new Exception();
    mockError(first, e);
    mockSuccessful(then);

    executor.execute(event, parameters, context, callback);

    InOrder inOrder = inOrder(first, then, callback);
    inOrder.verify(first).execute(same(event), same(parameters), same(context), any());
    inOrder.verify(then).execute(same(event), same(parameters), same(context), any());
    inOrder.verify(callback).error(e);
  }

  @Test
  public void secondExecutorFails() {
    final Exception e = new Exception();
    mockSuccessful(first);
    mockError(then, e);

    executor.execute(event, parameters, context, callback);

    InOrder inOrder = inOrder(first, then, callback);
    inOrder.verify(first).execute(same(event), same(parameters), same(context), any());
    inOrder.verify(then).execute(same(event), same(parameters), same(context), any());
    inOrder.verify(callback).error(e);
  }

  @Test
  public void bothExecutorsFail() {
    final Exception e = new Exception();
    final Exception e2 = new Exception();

    mockError(first, e);
    mockError(then, e2);

    executor.execute(event, parameters, context, callback);

    InOrder inOrder = inOrder(first, then, callback);
    inOrder.verify(first).execute(same(event), same(parameters), same(context), any());
    inOrder.verify(then).execute(same(event), same(parameters), same(context), any());
    inOrder.verify(callback).error(e);
  }

  private void mockSuccessful(SourceCallbackExecutor executor) {
    doAnswer(invocationOnMock -> {
      CompletableCallback completableCallback = invocationOnMock.getArgument(3);
      completableCallback.complete(null);

      return null;
    }).when(executor).execute(same(event), same(parameters), same(context), any());
  }

  private void mockError(SourceCallbackExecutor executor, Throwable t) {
    doAnswer(invocationOnMock -> {
      CompletableCallback completableCallback = invocationOnMock.getArgument(3);
      completableCallback.error(t);

      return null;
    }).when(executor).execute(same(event), same(parameters), same(context), any());
  }
}
