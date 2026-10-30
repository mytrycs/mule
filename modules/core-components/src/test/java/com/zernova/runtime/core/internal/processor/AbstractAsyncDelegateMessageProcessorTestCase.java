/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor;

import static java.lang.Thread.currentThread;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.locks.LockSupport.parkNanos;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static com.zernova.runtime.api.component.location.ConfigurationComponentLocator.REGISTRY_KEY;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.util.concurrent.Latch;
import com.zernova.runtime.core.api.construct.Flow;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.privileged.processor.chain.DefaultMessageProcessorChainBuilder;
import com.zernova.tck.junit4.AbstractReactiveProcessorTestCase;

import java.beans.ExceptionListener;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

public abstract class AbstractAsyncDelegateMessageProcessorTestCase extends AbstractReactiveProcessorTestCase
    implements ExceptionListener {

  protected AsyncDelegateMessageProcessor async;
  protected TestListener target = new TestListener();
  private Exception exceptionThrown;
  protected CountDownLatch latch = new Latch();
  protected Latch asyncEntryLatch = new Latch();
  protected Flow flow;

  @Rule
  public ExpectedException expected;

  public AbstractAsyncDelegateMessageProcessorTestCase(Mode mode) {
    super(mode);
    setStartContext(true);
  }

  @Override
  protected Map<String, Object> getStartUpRegistryObjects() {
    return singletonMap(REGISTRY_KEY, componentLocator);
  }

  @Override
  protected void doTearDown() throws Exception {
    async.stop();
    async.dispose();

    disposeIfNeeded(flow, getLogger(getClass()));
    super.doTearDown();
  }

  protected void park100ns() {
    parkNanos(100);
  }


  protected void assertTargetEvent(CoreEvent request) {
    // Assert that event is processed in async thread
    assertNotNull(target.sensedEvent);
    assertThat(request, not(sameInstance(target.sensedEvent)));
    assertThat(request.getCorrelationId(), equalTo(target.sensedEvent.getCorrelationId()));
    assertThat(request.getMessage(), sameInstance(target.sensedEvent.getMessage()));
    assertThat(target.thread, not(sameInstance(currentThread())));
  }

  protected void assertResponse(CoreEvent result) throws ZernovaException {
    // Assert that response is echoed by async and no exception is thrown in flow
    assertThat(testEvent(), sameInstance(result));
    assertThat(exceptionThrown, nullValue());
  }

  protected AsyncDelegateMessageProcessor createAsyncDelegateMessageProcessor(Processor listener, FlowConstruct flowConstruct)
      throws Exception {
    DefaultMessageProcessorChainBuilder delegateBuilder = new DefaultMessageProcessorChainBuilder();
    delegateBuilder.setProcessingStrategy(flowConstruct.getProcessingStrategy());
    delegateBuilder.chain(listener);

    AsyncDelegateMessageProcessor mp = new AsyncDelegateMessageProcessor(delegateBuilder, "thread");
    mp.setAnnotations(getAppleFlowComponentLocationAnnotations());
    initialiseIfNeeded(mp, true, muleContext);
    return mp;
  }

  class TestListener implements Processor {

    CoreEvent sensedEvent;
    Thread thread;

    @Override
    public CoreEvent process(CoreEvent event) throws ZernovaException {
      try {
        asyncEntryLatch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      sensedEvent = event;
      thread = currentThread();
      latch.countDown();
      return event;
    }
  }

  @Override
  public void exceptionThrown(Exception e) {
    exceptionThrown = e;
  }


}
