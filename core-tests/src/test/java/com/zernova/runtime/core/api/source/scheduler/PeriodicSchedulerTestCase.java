/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.source.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.concurrent.ScheduledFuture;

import org.junit.Test;

/**
 * Test to validate the interface {@link PeriodicScheduler} interface
 */
@SmallTest
public class PeriodicSchedulerTestCase extends AbstractZernovaTestCase {

  /**
   * If the {@link Scheduler} created is null then throw an {@link ScheduledPollCreationException}
   */
  @Test(expected = NullPointerException.class)
  public void checkCreationOfNullScheduler() {
    factory(null, null).schedule(null, this.newRunnable());
  }

  private PeriodicScheduler factory(ScheduledFuture schedulerToReturn, ZernovaContext muleContext) {
    PeriodicScheduler pollFactory = spy(PeriodicScheduler.class);
    when(pollFactory.doSchedule(any(), any())).thenReturn(schedulerToReturn);
    return pollFactory;
  }

  private Runnable newRunnable() {
    return () -> {
      // no-op
    };
  }
}
