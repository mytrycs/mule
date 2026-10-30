/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.processor.chain;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.newChain;

import static java.util.Optional.empty;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

import com.zernova.runtime.core.api.context.notification.ZernovaContextListener;
import com.zernova.runtime.core.internal.context.DefaultZernovaContext;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.testmodels.mule.TestMessageProcessor;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;



public class AbstractMessageProcessorChainTestCase extends AbstractZernovaTestCase {

  @InjectMocks
  MessageProcessorChain messageChain = newChain(empty(), new TestMessageProcessor("test"));

  @Mock
  DefaultZernovaContext muleContext;

  public AbstractMessageProcessorChainTestCase() {
    openMocks(this);
  }

  @Test
  public void testRemoveChain() throws Exception {
    startIfNeeded(messageChain);
    stopIfNeeded(messageChain);

    ArgumentCaptor<ZernovaContextListener> addListenerCaptor = ArgumentCaptor.forClass(ZernovaContextListener.class);
    ArgumentCaptor<ZernovaContextListener> removeListenerCaptor = ArgumentCaptor.forClass(ZernovaContextListener.class);

    verify(muleContext, times(1)).addListener(addListenerCaptor.capture());
    verify(muleContext, times(1)).removeListener(removeListenerCaptor.capture());

    assertThat(addListenerCaptor.getValue(), is(sameInstance(removeListenerCaptor.getValue())));
  }
}
