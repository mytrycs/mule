/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.construct;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.api.component.AbstractComponent.LOCATION_KEY;
import static com.zernova.runtime.dsl.api.component.config.DefaultComponentLocation.from;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.source.MessageSource;
import com.zernova.runtime.core.api.util.ObjectUtils;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import org.junit.Test;

public abstract class AbstractFlowConstructTestCase extends AbstractZernovaContextTestCase {

  protected static class DirectInboundMessageSource extends AbstractComponent implements MessageSource, Startable, Stoppable {

    private Processor listener;

    @Override
    public void setListener(Processor listener) {
      this.listener = listener;
    }

    public Processor getListener() {
      return listener;
    }

    @Override
    public String toString() {
      return ObjectUtils.toString(this);
    }

    @Override
    public void stop() throws ZernovaException {
      // Nothing to do
    }

    @Override
    public void start() throws ZernovaException {
      // Nothing to do
    }
  }

  protected DirectInboundMessageSource directInboundMessageSource;

  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();

    muleContext = spy(muleContext);
    directInboundMessageSource = new DirectInboundMessageSource();
  }

  protected abstract AbstractFlowConstruct getFlowConstruct() throws Exception;

  protected abstract AbstractFlowConstruct getStoppedFlowConstruct() throws Exception;

  @Test
  public void testStart() throws Exception {
    try {
      getFlowConstruct().start();
      fail("Exception expected: Cannot start an uninitialised service");
    } catch (final Exception e) {
      // expected
    }

    getFlowConstruct().setAnnotations(singletonMap(LOCATION_KEY, from("flow")));
    getFlowConstruct().initialise();
    getFlowConstruct().start();

    try {
      getFlowConstruct().initialise();
      fail("Exception expected: Cannot initialise an already initialised service");
    } catch (final IllegalStateException e) {
      // expected
    }
    getFlowConstruct().dispose();

  }

  @Test
  public void testStop() throws Exception {
    assertFalse(getFlowConstruct().isStarted());

    try {
      getFlowConstruct().stop();
      fail("Exception expected: Cannot stop an uninitialised service");
    } catch (final IllegalStateException e) {
      // expected
    }

    getFlowConstruct().setAnnotations(singletonMap(LOCATION_KEY, from("flow")));
    getFlowConstruct().initialise();
    assertFalse(getFlowConstruct().isStarted());

    // Can stop a service that is not started
    getFlowConstruct().stop();

    assertFalse(getFlowConstruct().isStarted());
    getFlowConstruct().start();
    assertTrue(getFlowConstruct().isStarted());
    getFlowConstruct().stop();
    assertFalse(getFlowConstruct().isStarted());
    try {
      getFlowConstruct().stop();
      fail("Exception expected: Cannot stop a service that is not started");
    } catch (final IllegalStateException e) {
      // expected
    }
    assertFalse(getFlowConstruct().isStarted());
    getFlowConstruct().dispose();

  }

  @Test
  public void testDispose() throws Exception {
    assertFalse(getFlowConstruct().isStarted());
    getFlowConstruct().dispose();

    try {
      getFlowConstruct().dispose();
      fail("Exception expected: Cannot dispose a service that is already disposed");
    } catch (final IllegalStateException e) {
      // expected
    }

    try {
      getFlowConstruct().initialise();
      fail("Exception expected: Cannot invoke initialise (or any lifecycle) on an object once it is disposed");
    } catch (final IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void testRegisterUnregister() throws ZernovaException, Exception {
    FlowConstruct construct = getFlowConstruct();
    construct.setAnnotations(singletonMap(LOCATION_KEY, from("flow")));
    ((ZernovaContextWithRegistry) muleContext).getRegistry().registerFlowConstruct(construct);
    assertNotNull(((ZernovaContextWithRegistry) muleContext).getRegistry().lookupFlowConstruct(construct.getName()));
  }

  @Test
  public void testInitialStateStopped() throws Exception {
    AbstractFlowConstruct flow = getStoppedFlowConstruct();
    flow.setAnnotations(singletonMap(LOCATION_KEY, from("flow")));

    assertFalse(flow.isStarted());
    assertFalse(flow.isStopped());

    flow.initialise();
    assertFalse(flow.isStarted());
    assertFalse(flow.isStopped());

    when(muleContext.isStarting()).thenReturn(true);
    // This should not actually start the flow
    flow.start();
    assertFalse(flow.isStarted());
    assertTrue(flow.isStopped());

    when(muleContext.isStarting()).thenReturn(false);
    // Finally the flow is actually started
    flow.start();
    assertTrue(flow.isStarted());
    assertFalse(flow.isStopped());

    // Try to start again
    try {
      flow.start();
      fail("Exception expected: Cannot start an already started flow");
    } catch (final IllegalStateException e) {
      // expected
    }
  }

}
