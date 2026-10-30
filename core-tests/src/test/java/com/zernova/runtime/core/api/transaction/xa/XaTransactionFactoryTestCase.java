/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.transaction.xa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.core.api.transaction.Transaction;
import com.zernova.runtime.core.internal.transaction.xa.XaTransactionFactory;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.Test;

import jakarta.transaction.TransactionManager;

public class XaTransactionFactoryTestCase extends AbstractZernovaTestCase {

  @Test
  public void setsTransactionTimeout() throws Exception {
    final int timeout = 1000;
    final TransactionManager transactionManager = mock(TransactionManager.class);
    final XaTransactionFactory transactionFactory = new XaTransactionFactory();
    transactionFactory.setTimeout(timeout);
    transactionFactory.setTransactionManager(transactionManager);

    final Transaction transaction = transactionFactory.beginTransaction("appName", mock(NotificationDispatcher.class));
    assertThat(transaction.getTimeout(), equalTo(timeout));
  }
}
