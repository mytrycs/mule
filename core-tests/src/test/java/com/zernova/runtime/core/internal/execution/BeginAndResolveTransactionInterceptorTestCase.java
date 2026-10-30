/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.execution;

import static com.zernova.runtime.core.privileged.transaction.TransactionConfig.ACTION_ALWAYS_BEGIN;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.core.api.execution.ExecutionCallback;
import com.zernova.runtime.core.api.transaction.Transaction;
import com.zernova.runtime.core.api.transaction.TransactionCoordination;
import com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig;
import com.zernova.runtime.core.privileged.exception.MessagingException;
import com.zernova.runtime.core.privileged.transaction.TransactionFactory;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.qameta.allure.Issue;

@SmallTest
public class BeginAndResolveTransactionInterceptorTestCase extends AbstractZernovaTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Mock
  ExecutionInterceptor executionInterceptor;

  @Mock
  ExecutionCallback executionCallback;

  @Mock
  ExecutionContext executionContext;

  @Mock
  TransactionFactory transactionFactory;

  @Mock
  MessagingException messagingException;

  BeginAndResolveTransactionInterceptor beginAndResolveTransactionInterceptor;

  ZernovaTransactionConfig transactionConfig;

  @Before
  public void before() {
    transactionConfig = new ZernovaTransactionConfig();
    beginAndResolveTransactionInterceptor =
        new BeginAndResolveTransactionInterceptor(executionInterceptor, transactionConfig, "APP", null, true, true, true);
  }

  @Test
  @Issue("MULE-19418")
  public void executeWithException() throws Exception {
    Transaction tx = spy(Transaction.class);

    transactionConfig.setAction(ACTION_ALWAYS_BEGIN);
    transactionConfig.setFactory(transactionFactory);
    when(transactionFactory.beginTransaction(any(), any())).thenReturn(tx);
    when(executionInterceptor.execute(executionCallback, executionContext)).thenThrow(messagingException);

    try {
      TransactionCoordination.getInstance().bindTransaction(tx);
      beginAndResolveTransactionInterceptor.execute(executionCallback, executionContext);
    } catch (MessagingException messagingException1) {
    }
    verify(tx, times(1)).rollback();
  }

}
