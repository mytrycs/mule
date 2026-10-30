/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.streaming;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.internal.streaming.IdentifiableCursorProviderDecorator.of;
import static com.zernova.tck.probe.PollingProber.DEFAULT_POLLING_INTERVAL;
import static com.zernova.tck.probe.PollingProber.check;

import static java.lang.System.gc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.streaming.bytes.CursorStreamProvider;
import com.zernova.runtime.core.internal.streaming.bytes.ManagedCursorStreamProvider;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import io.qameta.allure.Issue;
import org.junit.Test;

public class EventStreamingStateTestCase extends AbstractZernovaContextTestCase {

  private static final int GC_POLLING_TIMEOUT = 10000;

  private EventStreamingState eventStreamingState;

  private StreamingGhostBuster ghostBuster;

  @Override
  protected void doSetUp() throws Exception {
    ghostBuster = new StreamingGhostBuster();
    initialiseIfNeeded(ghostBuster, true, muleContext);
    startIfNeeded(ghostBuster);
    eventStreamingState = new EventStreamingState();
  }

  @Override
  protected void doTearDown() throws ZernovaException {
    ghostBuster.stop();
    ghostBuster.dispose();
  }

  @Test
  @Issue("W-13066586")
  public void invalidateInCacheWhenReferenceIsCollected() {
    MutableStreamingStatistics statistics = mock(MutableStreamingStatistics.class);
    IdentifiableCursorProvider provider = of(mock(CursorStreamProvider.class));
    int id = provider.getId();
    ManagedCursorStreamProvider managedCursorProvider = new ManagedCursorStreamProvider(provider, statistics);
    eventStreamingState.addProvider(managedCursorProvider, ghostBuster);

    // Force GC collection
    managedCursorProvider = null;

    check(GC_POLLING_TIMEOUT, DEFAULT_POLLING_INTERVAL, () -> {
      gc();
      assertNull(eventStreamingState.providers.getIfPresent(id));
      return true;
    });
  }

}
