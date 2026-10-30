/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transaction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.core.api.transaction.Transaction;
import com.zernova.runtime.core.api.transaction.TransactionCoordination;
import com.zernova.runtime.core.internal.context.notification.DefaultNotificationDispatcher;
import com.zernova.runtime.core.privileged.transaction.TransactionConfig;
import com.zernova.runtime.core.privileged.transaction.TransactionFactory;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.testmodels.mule.TestTransaction;

import org.junit.Test;

public class IsTransactedTestCase extends AbstractZernovaTestCase {

  @Test
  public void testIsTransacted() throws Exception {
    ZernovaTransactionConfig cfg = new ZernovaTransactionConfig();
    TestTransaction testTx = new TestTransaction("appName", new DefaultNotificationDispatcher());

    cfg.setAction(TransactionConfig.ACTION_NEVER);
    assertFalse(cfg.isTransacted());

    cfg.setAction(TransactionConfig.ACTION_NEVER);
    assertFalse(cfg.isTransacted());

    cfg.setFactory(new TransactedFactory());
    cfg.setAction(TransactionConfig.ACTION_ALWAYS_BEGIN);
    assertTrue(cfg.isTransacted());
    cfg.setAction(TransactionConfig.ACTION_ALWAYS_JOIN);
    assertTrue(cfg.isTransacted());
    cfg.setAction(TransactionConfig.ACTION_BEGIN_OR_JOIN);
    assertTrue(cfg.isTransacted());
    cfg.setAction(TransactionConfig.ACTION_JOIN_IF_POSSIBLE);
    assertFalse(cfg.isTransacted());
    TransactionCoordination.getInstance().bindTransaction(testTx);
    assertTrue(cfg.isTransacted());
    TransactionCoordination.getInstance().unbindTransaction(testTx);
    cfg.setAction(TransactionConfig.ACTION_INDIFFERENT);
    assertFalse(cfg.isTransacted());
    TransactionCoordination.getInstance().bindTransaction(testTx);
    assertTrue(cfg.isTransacted());
    TransactionCoordination.getInstance().unbindTransaction(testTx);

    cfg.setFactory(new NonTransactedFactory());
    cfg.setAction(TransactionConfig.ACTION_ALWAYS_BEGIN);
    assertFalse(cfg.isTransacted());
    cfg.setAction(TransactionConfig.ACTION_ALWAYS_JOIN);
    assertFalse(cfg.isTransacted());
    cfg.setAction(TransactionConfig.ACTION_BEGIN_OR_JOIN);
    assertFalse(cfg.isTransacted());
    cfg.setAction(TransactionConfig.ACTION_JOIN_IF_POSSIBLE);
    assertFalse(cfg.isTransacted());
    TransactionCoordination.getInstance().bindTransaction(testTx);
    assertFalse(cfg.isTransacted());
    TransactionCoordination.getInstance().unbindTransaction(testTx);
    cfg.setAction(TransactionConfig.ACTION_INDIFFERENT);
    assertFalse(cfg.isTransacted());
    TransactionCoordination.getInstance().bindTransaction(testTx);
    assertFalse(cfg.isTransacted());
    TransactionCoordination.getInstance().unbindTransaction(testTx);
  }

  @Test(expected = ZernovaRuntimeException.class)
  public void testExpectException1() {
    ZernovaTransactionConfig cfg = new ZernovaTransactionConfig(TransactionConfig.ACTION_ALWAYS_BEGIN);
    cfg.isTransacted();
  }

  @Test(expected = ZernovaRuntimeException.class)
  public void testExpectException2() {
    ZernovaTransactionConfig cfg = new ZernovaTransactionConfig(TransactionConfig.ACTION_JOIN_IF_POSSIBLE);
    cfg.isTransacted();
  }

  public static class TransactedFactory implements TransactionFactory {

    @Override
    public Transaction beginTransaction(String applicationName, NotificationDispatcher notificationFirer) {
      return null;
    }

    @Override
    public boolean isTransacted() {
      return true;
    }
  }

  public static class NonTransactedFactory implements TransactionFactory {

    @Override
    public Transaction beginTransaction(String applicationName, NotificationDispatcher notificationFirer) {
      return null;
    }

    @Override
    public boolean isTransacted() {
      return false;
    }
  }

}
