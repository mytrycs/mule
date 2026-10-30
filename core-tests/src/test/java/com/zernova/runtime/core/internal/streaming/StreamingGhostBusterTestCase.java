/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.streaming;

import static com.zernova.runtime.core.api.alert.ZernovaAlertingSupport.AlertNames.ALERT_NOT_CONSUMED_STREAM_BUSTED;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.internal.streaming.IdentifiableCursorProviderDecorator.of;
import static com.zernova.runtime.dsl.api.component.config.DefaultComponentLocation.from;
import static com.zernova.tck.probe.PollingProber.DEFAULT_POLLING_INTERVAL;
import static com.zernova.tck.probe.PollingProber.check;
import static com.zernova.test.allure.AllureConstants.StreamingFeature.STREAMING;
import static com.zernova.test.allure.AllureConstants.StreamingFeature.StreamingStory.STREAM_MANAGEMENT;
import static com.zernova.test.allure.AllureConstants.SupportabilityFeature.SUPPORTABILITY;
import static com.zernova.test.allure.AllureConstants.SupportabilityFeature.SupportabilityStory.ALERTS;

import static java.lang.System.gc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.alert.AlertingSupport;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.streaming.bytes.CursorStreamProvider;
import com.zernova.runtime.core.internal.streaming.StreamingGhostBuster.StreamingWeakReference;
import com.zernova.runtime.core.internal.streaming.bytes.ManagedCursorStreamProvider;
import com.zernova.tck.SimpleUnitTestSupportSchedulerService;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.lang.ref.WeakReference;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@Feature(STREAMING)
@Story(STREAM_MANAGEMENT)
class StreamingGhostBusterTestCase extends AbstractZernovaTestCase {

  private static final int GC_POLLING_TIMEOUT = 10000;

  private AlertingSupport alertingSupport;

  private StreamingGhostBuster ghostBuster;

  @BeforeEach
  public void setUp() throws Exception {
    alertingSupport = mock(AlertingSupport.class);
    ghostBuster = new StreamingGhostBuster();
    ghostBuster.setSchedulerService(new SimpleUnitTestSupportSchedulerService());
    ghostBuster.setAlertingSupport(alertingSupport);
    initialiseIfNeeded(ghostBuster);
    startIfNeeded(ghostBuster);
  }

  @AfterEach
  public void tearDown() throws ZernovaException {
    ghostBuster.stop();
    ghostBuster.dispose();
  }

  @Test
  @Issue("MULE-18573")
  void releaseResourcesWhenReferenceIsCollected() {
    MutableStreamingStatistics statistics = mock(MutableStreamingStatistics.class);
    CursorStreamProvider provider = mock(CursorStreamProvider.class);
    ManagedCursorStreamProvider managedCursorProvider = new ManagedCursorStreamProvider(of(provider), statistics);

    WeakReference<ManagedCursorProvider> reference = ghostBuster.track(managedCursorProvider);

    // Force GC collection
    managedCursorProvider = null;

    check(GC_POLLING_TIMEOUT, DEFAULT_POLLING_INTERVAL, () -> {
      gc();
      assertThat(reference.get(), is(nullValue()));
      verify(provider).releaseResources();
      return true;
    });
  }

  @Test
  @Feature(SUPPORTABILITY)
  @Story(ALERTS)
  void collectedReferenceNotClosedTriggersAlert() {
    MutableStreamingStatistics statistics = mock(MutableStreamingStatistics.class);
    CursorStreamProvider provider = mock(CursorStreamProvider.class);
    when(provider.getOriginatingLocation()).thenReturn(Optional.of(from("streamGenerator")));
    ManagedCursorStreamProvider managedCursorProvider = new ManagedCursorStreamProvider(of(provider), statistics);

    WeakReference<ManagedCursorProvider> reference = ghostBuster.track(managedCursorProvider);

    // Force GC collection
    managedCursorProvider = null;

    check(GC_POLLING_TIMEOUT, DEFAULT_POLLING_INTERVAL, () -> {
      gc();
      assertThat(reference.get(), is(nullValue()));
      verify(alertingSupport).triggerAlert(ALERT_NOT_CONSUMED_STREAM_BUSTED, "streamGenerator");
      return true;
    });
  }

  @Test
  @Feature(SUPPORTABILITY)
  @Story(ALERTS)
  void collectedReferencePreviouslyClosedNotTriggersAlert() {
    MutableStreamingStatistics statistics = mock(MutableStreamingStatistics.class);
    CursorStreamProvider provider = mock(CursorStreamProvider.class);
    when(provider.getOriginatingLocation()).thenReturn(Optional.of(from("streamGenerator")));
    ManagedCursorStreamProvider managedCursorProvider = new ManagedCursorStreamProvider(of(provider), statistics);
    managedCursorProvider.releaseResources();

    StreamingWeakReference reference = (StreamingWeakReference) ghostBuster.track(managedCursorProvider);
    reference.dispose();
    verify(alertingSupport, never()).triggerAlert(any());
    verify(alertingSupport, never()).triggerAlert(any(), any());
  }

}
