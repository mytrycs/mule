/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.listener;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;

import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.util.concurrent.Latch;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.api.notification.ExceptionNotification;
import com.zernova.runtime.api.notification.ExceptionNotificationListener;
import com.zernova.runtime.api.notification.NotificationListenerRegistry;
import com.zernova.runtime.core.api.exception.RollbackSourceCallback;
import com.zernova.runtime.core.api.exception.SystemExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Listener for exceptions managed by the {@link com.zernova.runtime.core.api.exception.SystemExceptionHandler}.
 */
public class SystemExceptionListener {

  private static final Logger logger = LoggerFactory.getLogger(SystemExceptionListener.class);

  private CountDownLatch exceptionThrownLatch = new Latch();
  private int timeout = 10000;
  private List<ExceptionNotification> exceptionNotifications = new ArrayList<>();
  private AtomicInteger numberOfInvocations = new AtomicInteger();

  public SystemExceptionListener(ZernovaContext muleContext, NotificationListenerRegistry notificationListenerRegistry) {
    final SystemExceptionHandler exceptionListener = muleContext.getExceptionListener();
    muleContext.setExceptionListener(new CountingSystemExceptionHandler(exceptionListener));
    notificationListenerRegistry
        .registerListener((ExceptionNotificationListener) notification -> exceptionNotifications.add(notification));
  }

  public SystemExceptionListener waitUntilAllNotificationsAreReceived() {
    try {
      if (!exceptionThrownLatch.await(timeout, MILLISECONDS)) {
        fail("An exception was never thrown");
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  /**
   * @param numberOfExecutionsRequired number of times that the listener must be notified before releasing the latch.
   */
  public SystemExceptionListener setNumberOfExecutionsRequired(int numberOfExecutionsRequired) {
    this.exceptionThrownLatch = new CountDownLatch(numberOfExecutionsRequired);
    return this;
  }

  /**
   * @param timeout milliseconds to wait when calling {@link #waitUntilAllNotificationsAreReceived()} for an exception to be
   *                handled
   */
  public SystemExceptionListener setTimeoutInMillis(int timeout) {
    this.timeout = timeout;
    return this;
  }

  /**
   * Asserts that the system exception handler was not invoked.
   */
  public void assertNotInvoked() {
    assertThat(this.numberOfInvocations.get(), is(0));
  }

  private class CountingSystemExceptionHandler implements SystemExceptionHandler, Disposable {

    private final SystemExceptionHandler exceptionListener;

    private CountingSystemExceptionHandler(SystemExceptionHandler exceptionListener) {
      this.exceptionListener = exceptionListener;
    }

    @Override
    public void handleException(Exception exception, RollbackSourceCallback rollbackMethod) {
      try {
        numberOfInvocations.incrementAndGet();
        exceptionListener.handleException(exception, rollbackMethod);
      } finally {
        exceptionThrownLatch.countDown();
      }
    }

    @Override
    public void handleException(Exception exception) {
      try {
        numberOfInvocations.incrementAndGet();
        exceptionListener.handleException(exception);
      } finally {
        exceptionThrownLatch.countDown();
      }
    }

    @Override
    public void dispose() {
      disposeIfNeeded(exceptionListener, logger);
    }
  }
}
