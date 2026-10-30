/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transaction;

import static com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig.ACTION_ALWAYS_BEGIN;
import static com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig.ACTION_ALWAYS_BEGIN_STRING;
import static com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig.ACTION_ALWAYS_JOIN;
import static com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig.ACTION_ALWAYS_JOIN_STRING;
import static com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig.ACTION_BEGIN_OR_JOIN;
import static com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig.ACTION_BEGIN_OR_JOIN_STRING;
import static com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig.ACTION_INDIFFERENT;
import static com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig.ACTION_INDIFFERENT_STRING;
import static com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig.ACTION_JOIN_IF_POSSIBLE;
import static com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig.ACTION_JOIN_IF_POSSIBLE_STRING;
import static com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig.ACTION_NONE;
import static com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig.ACTION_NONE_STRING;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.core.privileged.transaction.TransactionFactory;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.testmodels.mule.TestTransactionFactory;

import org.junit.Test;

public class ZernovaTransactionConfigTestCase extends AbstractZernovaContextTestCase {

  @Test
  public void testActionAndStringConversion() {
    ZernovaTransactionConfig c = new ZernovaTransactionConfig();
    c.setMuleContext(muleContext);

    c.setAction(ACTION_ALWAYS_BEGIN);
    assertEquals(ACTION_ALWAYS_BEGIN_STRING, c.getActionAsString());

    c.setAction(ACTION_ALWAYS_JOIN);
    assertEquals(ACTION_ALWAYS_JOIN_STRING, c.getActionAsString());

    c.setAction(ACTION_BEGIN_OR_JOIN);
    assertEquals(ACTION_BEGIN_OR_JOIN_STRING, c.getActionAsString());

    c.setAction(ACTION_JOIN_IF_POSSIBLE);
    assertEquals(ACTION_JOIN_IF_POSSIBLE_STRING, c.getActionAsString());

    c.setAction(ACTION_NONE);
    assertEquals(ACTION_NONE_STRING, c.getActionAsString());

    c.setAction(ACTION_INDIFFERENT);
    assertEquals(ACTION_INDIFFERENT_STRING, c.getActionAsString());
  }

  @Test
  public void testDefaults() throws Exception {
    ZernovaTransactionConfig c = new ZernovaTransactionConfig(ACTION_ALWAYS_BEGIN);
    c.setMuleContext(muleContext);
    assertEquals("Wrong default TX timeout", 30000, c.getTimeout());
  }

  @Test
  public void testTransactionJoinIfPossible() throws TransactionException {
    ZernovaTransactionConfig txConfig = new ZernovaTransactionConfig(ACTION_JOIN_IF_POSSIBLE);
    txConfig.setMuleContext(muleContext);
    txConfig.setFactory(new TestTransactionFactory());
    assertFalse(txConfig.isTransacted());
  }

  @Test
  public void testFailNoFactory() {
    ZernovaTransactionConfig txConfig = new ZernovaTransactionConfig(ACTION_ALWAYS_BEGIN);
    txConfig.setMuleContext(muleContext);
    // note how we don't set a factory here so the default in MTC is null

    try {
      txConfig.isTransacted();
      fail("isTransacted() must fail if no factory is set");
    } catch (RuntimeException re) {
      // this was expected
    }
  }

  @Test
  public void testEquals() {
    ZernovaTransactionConfig config = new ZernovaTransactionConfig();
    assertThat(config.equals(config), is(true));
    assertThat(config.equals(null), is(false));
    assertThat(config.equals("foo"), is(false));
    ZernovaTransactionConfig config2 = new ZernovaTransactionConfig();

    TransactionFactory factory = new TestTransactionFactory();
    config.setFactory(factory);
    config2.setFactory(factory);
    config.setActionAsString(ACTION_ALWAYS_BEGIN_STRING);
    config2.setActionAsString(ACTION_ALWAYS_BEGIN_STRING);

    assertThat(config.equals(config2), is(true));
  }

  @Test(expected = ZernovaRuntimeException.class)
  public void failingWithoutFactory() {
    ZernovaTransactionConfig config = new ZernovaTransactionConfig();
    config.setActionAsString(ACTION_ALWAYS_BEGIN_STRING);
    config.isTransacted();
  }
}
