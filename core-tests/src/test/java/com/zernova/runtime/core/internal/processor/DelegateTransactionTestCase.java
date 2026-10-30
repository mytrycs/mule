/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor;

import static com.zernova.tck.util.ZernovaContextUtils.mockMuleContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.core.internal.transaction.DelegateTransaction;
import com.zernova.runtime.core.privileged.registry.RegistrationException;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.util.ZernovaContextUtils;

import org.junit.Before;
import org.junit.Test;

public class DelegateTransactionTestCase extends AbstractZernovaTestCase {

  private static final int DEFAULT_TX_TIMEOUT = 30000;

  private final String applicationName = "appName";
  private NotificationDispatcher notificationDispatcher;

  @Before
  public void detUp() throws RegistrationException {
    notificationDispatcher = ZernovaContextUtils.getNotificationDispatcher(mockMuleContext());
  }

  @Test
  public void defaultTxTimeout() {
    DelegateTransaction delegateTransaction = new DelegateTransaction(applicationName, notificationDispatcher);
    assertThat(delegateTransaction.getTimeout(), is(DEFAULT_TX_TIMEOUT));
  }

  @Test
  public void changeTxTimeout() {
    DelegateTransaction delegateTransaction = new DelegateTransaction(applicationName, notificationDispatcher);
    int newTimeout = 10;
    delegateTransaction.setTimeout(newTimeout);
    assertThat(delegateTransaction.getTimeout(), is(newTimeout));
  }
}
