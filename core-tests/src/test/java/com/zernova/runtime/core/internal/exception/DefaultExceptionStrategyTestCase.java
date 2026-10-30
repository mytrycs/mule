/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.exception;

import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;
import static com.zernova.runtime.api.component.AbstractComponent.LOCATION_KEY;
import static com.zernova.runtime.api.notification.AbstractServerNotification.TYPE_ERROR;
import static com.zernova.runtime.api.notification.ExceptionNotification.EXCEPTION_ACTION;
import static com.zernova.tck.util.ZernovaContextUtils.getNotificationDispatcher;

import com.zernova.runtime.api.notification.ExceptionNotificationListener;
import com.zernova.runtime.api.notification.IntegerAction;
import com.zernova.runtime.api.notification.NotificationListenerRegistry;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultExceptionStrategyTestCase extends AbstractZernovaContextTestCase {

  // MULE-1404
  @Test
  public void testExceptions() throws Exception {
    InstrumentedExceptionStrategy strategy = new InstrumentedExceptionStrategy(muleContext);
    strategy.setMuleContext(muleContext);
    strategy
        .setNotificationFirer(getNotificationDispatcher(muleContext));
    strategy.setAnnotations(singletonMap(LOCATION_KEY, TEST_CONNECTOR_LOCATION));
    strategy.handleException(new IllegalArgumentException("boom"));
    assertEquals(1, strategy.getCount());
  }

  // MULE-1627
  @Test
  public void testExceptionNotifications() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicInteger notificationCount = new AtomicInteger(0);

    ((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObject(NotificationListenerRegistry.class)
        .registerListener((ExceptionNotificationListener) notification -> {
          if (new IntegerAction(EXCEPTION_ACTION).equals(notification.getAction())) {
            assertEquals("exception", notification.getActionName());
            assertEquals("Wrong info type", TYPE_ERROR, notification.getType());
            notificationCount.incrementAndGet();
            latch.countDown();
          }
        });

    // throwing exception
    InstrumentedExceptionStrategy strategy = new InstrumentedExceptionStrategy(muleContext);
    strategy.setAnnotations(singletonMap(LOCATION_KEY, TEST_CONNECTOR_LOCATION));
    strategy.setMuleContext(muleContext);
    strategy
        .setNotificationFirer(getNotificationDispatcher(muleContext));
    strategy.handleException(new IllegalArgumentException("boom"));

    // Wait for the notifcation event to be fired as they are queue
    latch.await(2000, MILLISECONDS);
    assertEquals(1, notificationCount.get());

  }

  private static class InstrumentedExceptionStrategy extends DefaultSystemExceptionStrategy {

    private volatile int count = 0;

    public InstrumentedExceptionStrategy(ZernovaContext muleContext) {
      super();
    }

    @Override
    public void handleException(Exception exception) {
      count++;
      super.handleException(exception);
    }

    public int getCount() {
      return count;
    }
  }
}
