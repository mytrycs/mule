/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.management.stats;

import com.zernova.runtime.core.api.management.stats.ResetOnQueryCounter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of {@link ResetOnQueryCounter} that holds the counter in an {@link AtomicLong}.
 *
 * @since 4.5
 */
public class DefaultResetOnQueryCounter implements ResetOnQueryCounter {

  private final AtomicLong counter = new AtomicLong(0);

  @Override
  public long getAndReset() {
    return counter.getAndSet(0);
  }

  @Override
  public long get() {
    return counter.get();
  }

  public void increment() {
    counter.incrementAndGet();
  }

  public void add(long value) {
    counter.addAndGet(value);
  }
}
