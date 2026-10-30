/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.transaction;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;
import static com.zernova.runtime.api.notification.TransactionNotification.TRANSACTION_BEGAN;
import static com.zernova.runtime.api.notification.TransactionNotification.TRANSACTION_COMMITTED;
import static com.zernova.runtime.api.notification.TransactionNotification.TRANSACTION_ROLLEDBACK;
import static com.zernova.tck.util.ZernovaContextUtils.getNotificationDispatcher;

import com.zernova.runtime.api.notification.IntegerAction;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.api.notification.NotificationListenerRegistry;
import com.zernova.runtime.api.notification.TransactionNotification;
import com.zernova.runtime.api.notification.TransactionNotificationListener;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.transaction.AbstractSingleResourceTransaction;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

public class TransactionNotificationsTestCase extends AbstractZernovaContextTestCase {

  @Test
  public void testTransactionNotifications() throws Exception {
    final CountDownLatch latch = new CountDownLatch(3);

    // the code is simple and deceptive :) The trick is this dummy transaction is handled by
    // a global TransactionCoordination instance, which binds it to the current thread.
    Transaction transaction =
        new DummyTransaction("appName",
                             getNotificationDispatcher(muleContext));

    ((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObject(NotificationListenerRegistry.class)
        .registerListener(new TransactionNotificationListener<TransactionNotification>() {

          @Override
          public boolean isBlocking() {
            return false;
          }

          @Override
          public void onNotification(TransactionNotification notification) {
            if (new IntegerAction(TRANSACTION_BEGAN).equals(notification.getAction())) {
              assertEquals("begin", notification.getActionName());
              latch.countDown();
            } else if (new IntegerAction(TRANSACTION_COMMITTED).equals(notification.getAction())) {
              assertEquals("commit", notification.getActionName());
              latch.countDown();
            } else if (new IntegerAction(TRANSACTION_ROLLEDBACK).equals(notification.getAction())) {
              assertEquals("rollback", notification.getActionName());
              latch.countDown();
            }
          }
        }, notification -> transaction.getId().equals(notification.getResourceIdentifier()));

    transaction.begin();
    transaction.commit();
    transaction.rollback();

    // Wait for the notification event to be fired as they are queued
    latch.await(2000, MILLISECONDS);

    assertEquals("There are still some notifications left unfired.", 0, latch.getCount());
  }

  private class DummyTransaction extends AbstractSingleResourceTransaction {

    private DummyTransaction(String applicationName,
                             NotificationDispatcher notificationFirer) {
      super(applicationName, notificationFirer);
    }

    @Override
    protected Class getResourceType() {
      return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected Class getKeyType() {
      return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void doBegin() {

    }

    @Override
    protected void doCommit() {

    }

    @Override
    protected void doRollback() {

    }
  }

}
