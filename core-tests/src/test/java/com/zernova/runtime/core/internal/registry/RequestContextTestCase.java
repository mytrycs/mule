/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.registry;

import static org.junit.Assert.assertEquals;
import static com.zernova.runtime.core.privileged.event.PrivilegedEvent.setCurrentEvent;
import static com.zernova.tck.ZernovaTestUtils.createErrorMock;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.privileged.event.PrivilegedEvent;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.Test;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public class RequestContextTestCase extends AbstractZernovaTestCase {

  private static final Logger LOGGER = getLogger(RequestContextTestCase.class);

  @Test
  public void testSetExceptionPayloadAcrossThreads() throws InterruptedException, ZernovaException {
    runThread(testEvent(), false);
    runThread(testEvent(), true);
  }

  @Test
  public void testFailureWithoutThreadSafeEvent() throws InterruptedException, ZernovaException {
    runThread(testEvent(), false);
    runThread(testEvent(), true);
  }

  protected void runThread(CoreEvent event, boolean doTest) throws InterruptedException {
    AtomicBoolean success = new AtomicBoolean(false);
    Thread thread = new Thread(new SetExceptionPayload((PrivilegedEvent) event, success));
    thread.start();
    thread.join();
    if (doTest) {
      // Since events are now immutable, there should be no failures due to this!
      assertEquals(true, success.get());
    }
  }

  private class SetExceptionPayload implements Runnable {

    private PrivilegedEvent event;
    private AtomicBoolean success;

    public SetExceptionPayload(PrivilegedEvent event, AtomicBoolean success) {
      this.event = event;
      this.success = success;
    }

    @Override
    public void run() {
      try {
        Exception exception = new Exception();
        event = PrivilegedEvent.builder(event)
            .error(createErrorMock(exception)).build();
        setCurrentEvent(event);
        success.set(true);
      } catch (RuntimeException e) {
        LOGGER.error("error in thread", e);
      }
    }
  }
}
