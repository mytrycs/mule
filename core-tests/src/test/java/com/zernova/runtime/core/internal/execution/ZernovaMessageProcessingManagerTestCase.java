/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.execution;

import static java.lang.Thread.currentThread;
import static java.util.Optional.of;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.exception.SystemExceptionHandler;
import com.zernova.runtime.core.internal.policy.PolicyManager;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.size.SmallTest;

import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@SmallTest
public class ZernovaMessageProcessingManagerTestCase extends AbstractZernovaContextTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Mock
  private SystemExceptionHandler exceptionHandler;

  @Mock
  private PolicyManager policyManager;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private FlowProcessTemplate template;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private MessageProcessContext messageProcessContext;

  private ZernovaContext spyContext;

  private ZernovaMessageProcessingManager processingManager;

  @Before
  public void setUp() throws Exception {
    spyContext = spy(muleContext);
    when(spyContext.getExceptionListener()).thenReturn(exceptionHandler);

    processingManager = new ZernovaMessageProcessingManager();
    processingManager.setMuleContext(spyContext);
    processingManager.setPolicyManager(of(policyManager));

    processingManager.initialise();
  }

  @Test
  public void processesWithClassLoader() {
    ClassLoader classLoader = new URLClassLoader(new URL[] {}, currentThread().getContextClassLoader());
    when(messageProcessContext.getExecutionClassLoader()).thenReturn(classLoader);

    doAnswer(inv -> {
      exceptionHandler.handleException(inv.getArgument(0));
      return null;
    }).when(template).sendFailureResponseToClient(any(), any(), any());

    doAnswer(inv -> {
      assertThat(currentThread().getContextClassLoader(), is(sameInstance(classLoader)));
      return null;
    }).when(exceptionHandler).handleException(any());

    processingManager.processMessage(template, messageProcessContext);
    verify(exceptionHandler).handleException(any());
  }

  @Test
  public void exceptionHandlerInvoked() {
    Exception e = new Exception();
    processingManager.phaseFailure(e);
    verify(exceptionHandler).handleException(e);
  }
}
