/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.execution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.mock;

import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.transaction.Transaction;
import com.zernova.runtime.core.api.transaction.TransactionCoordination;
import com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig;
import com.zernova.runtime.core.internal.transaction.xa.IllegalTransactionStateException;
import com.zernova.runtime.core.privileged.transaction.TransactionConfig;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@SmallTest
public class ValidateTransactionalStateInterceptorTestCase extends AbstractZernovaTestCase {

  public static final ZernovaTransactionConfig MULE_TRANSACTION_CONFIG_ALWAYS_BEGIN =
      new ZernovaTransactionConfig(TransactionConfig.ACTION_ALWAYS_BEGIN);
  public static final ZernovaTransactionConfig MULE_TRANSACTION_CONFIG_BEGIN_OR_JOIN =
      new ZernovaTransactionConfig(TransactionConfig.ACTION_BEGIN_OR_JOIN);
  public static final ZernovaTransactionConfig MULE_TRANSACTION_CONFIG_ALWAYS_JOIN =
      new ZernovaTransactionConfig(TransactionConfig.ACTION_ALWAYS_JOIN);
  public static final ZernovaTransactionConfig MULE_TRANSACTION_CONFIG_INDIFFERENT =
      new ZernovaTransactionConfig(TransactionConfig.ACTION_INDIFFERENT);
  public static final ZernovaTransactionConfig MULE_TRANSACTION_CONFIG_JOIN_IF_POSSIBLE =
      new ZernovaTransactionConfig(TransactionConfig.ACTION_JOIN_IF_POSSIBLE);
  public static final ZernovaTransactionConfig MULE_TRANSACTION_CONFIG_NEVER =
      new ZernovaTransactionConfig(TransactionConfig.ACTION_NEVER);
  public static final ZernovaTransactionConfig MULE_TRANSACTION_CONFIG_NONE =
      new ZernovaTransactionConfig(TransactionConfig.ACTION_NONE);

  private static final Map<Boolean, Map<ZernovaTransactionConfig, Boolean>> resultMap =
      new HashMap<>();
  private boolean hasTransactionInContext;
  private TransactionConfig transactionConfig;
  private CoreEvent mockMuleEvent = mock(CoreEvent.class);
  private Transaction mockTransaction = mock(Transaction.class);

  @Parameterized.Parameters(name = "TX in Contex: {0}. TX Config: {1}")
  public static Collection<Object[]> parameters() {
    return Arrays
        .asList(new Object[][] {{false, MULE_TRANSACTION_CONFIG_ALWAYS_BEGIN}, {false, MULE_TRANSACTION_CONFIG_ALWAYS_JOIN},
            {false, MULE_TRANSACTION_CONFIG_BEGIN_OR_JOIN}, {false, MULE_TRANSACTION_CONFIG_INDIFFERENT},
            {false, MULE_TRANSACTION_CONFIG_JOIN_IF_POSSIBLE}, {false, MULE_TRANSACTION_CONFIG_NEVER},
            {false, MULE_TRANSACTION_CONFIG_NONE},
            {true, MULE_TRANSACTION_CONFIG_ALWAYS_JOIN}, {true, MULE_TRANSACTION_CONFIG_BEGIN_OR_JOIN},
            {true, MULE_TRANSACTION_CONFIG_INDIFFERENT}, {true, MULE_TRANSACTION_CONFIG_JOIN_IF_POSSIBLE},
            {true, MULE_TRANSACTION_CONFIG_NEVER}, {true, MULE_TRANSACTION_CONFIG_NONE}});
  }

  static {
    Map<ZernovaTransactionConfig, Boolean> falseResultMap = new HashMap<>();
    Map<ZernovaTransactionConfig, Boolean> trueResultMap = new HashMap<>();
    resultMap.put(false, falseResultMap);
    resultMap.put(true, trueResultMap);
    falseResultMap.put(MULE_TRANSACTION_CONFIG_ALWAYS_BEGIN, false);
    falseResultMap.put(MULE_TRANSACTION_CONFIG_ALWAYS_JOIN, true);
    falseResultMap.put(MULE_TRANSACTION_CONFIG_BEGIN_OR_JOIN, false);
    falseResultMap.put(MULE_TRANSACTION_CONFIG_INDIFFERENT, false);
    falseResultMap.put(MULE_TRANSACTION_CONFIG_JOIN_IF_POSSIBLE, false);
    falseResultMap.put(MULE_TRANSACTION_CONFIG_NEVER, false);
    falseResultMap.put(MULE_TRANSACTION_CONFIG_NONE, false);

    trueResultMap.put(MULE_TRANSACTION_CONFIG_ALWAYS_BEGIN, false);
    trueResultMap.put(MULE_TRANSACTION_CONFIG_ALWAYS_JOIN, false);
    trueResultMap.put(MULE_TRANSACTION_CONFIG_BEGIN_OR_JOIN, false);
    trueResultMap.put(MULE_TRANSACTION_CONFIG_INDIFFERENT, false);
    trueResultMap.put(MULE_TRANSACTION_CONFIG_JOIN_IF_POSSIBLE, false);
    trueResultMap.put(MULE_TRANSACTION_CONFIG_NEVER, true);
    trueResultMap.put(MULE_TRANSACTION_CONFIG_NONE, false);
  }

  public ValidateTransactionalStateInterceptorTestCase(boolean hasTransactionInContext, TransactionConfig transactionConfig) {
    this.hasTransactionInContext = hasTransactionInContext;
    this.transactionConfig = transactionConfig;
  }

  @Before
  public void removeTransaction() {
    TransactionCoordination.getInstance().clear();
  }

  @Test
  public void testTransactionalState() throws Exception {
    boolean shouldThrowException = resultMap.get(hasTransactionInContext).get(transactionConfig);
    Exception thrownException = null;
    CoreEvent result = null;
    if (hasTransactionInContext) {
      TransactionCoordination.getInstance().bindTransaction(mockTransaction);
    }
    ValidateTransactionalStateInterceptor<CoreEvent> interceptor =
        new ValidateTransactionalStateInterceptor<>(new ExecuteCallbackInterceptor<CoreEvent>(),
                                                    transactionConfig, false);
    try {
      result = interceptor.execute(() -> mockMuleEvent, new ExecutionContext());
    } catch (IllegalTransactionStateException e) {
      thrownException = e;
    }
    if (shouldThrowException) {
      assertThat(thrownException, notNullValue());
      assertThat(thrownException, instanceOf(IllegalTransactionStateException.class));
    } else {
      assertThat(result, is(mockMuleEvent));
    }
  }
}
