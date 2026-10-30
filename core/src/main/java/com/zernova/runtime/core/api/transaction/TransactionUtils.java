/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.transaction;

import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.TX_START;
import static com.zernova.runtime.api.tx.TransactionType.LOCAL;
import static com.zernova.runtime.api.tx.TransactionType.XA;
import static com.zernova.runtime.core.api.transaction.TransactionCoordination.isTransactionActive;

import static java.lang.System.currentTimeMillis;
import static java.util.Optional.empty;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.profiling.ProfilingDataProducer;
import com.zernova.runtime.api.profiling.type.ProfilingEventType;
import com.zernova.runtime.api.profiling.type.TransactionProfilingEventType;
import com.zernova.runtime.api.profiling.type.context.TransactionProfilingEventContext;
import com.zernova.runtime.api.tx.TransactionType;
import com.zernova.runtime.core.internal.profiling.context.DefaultTransactionProfilingEventContext;
import com.zernova.runtime.core.internal.transaction.TransactionAdapter;

public final class TransactionUtils {

  private TransactionUtils() {

  }

  /**
   * Triggers a Profiling Event of type {@link TransactionProfilingEventType} if there is an ongoing transaction (or it is being
   * started with this event).
   */
  public static void profileTransactionAction(ProfilingDataProducer<TransactionProfilingEventContext, Object> dataProducer,
                                              ProfilingEventType<TransactionProfilingEventContext> type,
                                              ComponentLocation location) {
    if (!isTransactionActive() && !type.equals(TX_START)) {
      return;
    }
    Transaction tx = TransactionCoordination.getInstance().getTransaction();
    TransactionType txType = tx.isXA() ? XA : LOCAL;
    dataProducer.triggerProfilingEvent(new DefaultTransactionProfilingEventContext(
                                                                                   tx instanceof TransactionAdapter
                                                                                       ? ((TransactionAdapter) tx)
                                                                                           .getComponentLocation()
                                                                                       : empty(),
                                                                                   location, txType, currentTimeMillis()));
  }

}
