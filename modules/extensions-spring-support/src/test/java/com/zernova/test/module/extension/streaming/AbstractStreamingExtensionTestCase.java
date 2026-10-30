/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.streaming;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.api.streaming.StreamingStatistics;
import com.zernova.tck.probe.JUnitLambdaProbe;
import com.zernova.tck.probe.PollingProber;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import jakarta.inject.Inject;

abstract class AbstractStreamingExtensionTestCase extends AbstractExtensionFunctionalTestCase {

  @Inject
  private StreamingManager streamingManager;

  @Override
  protected void doTearDownAfterMuleContextDispose() throws Exception {
    assertAllStreamingResourcesClosed();
  }

  private void assertAllStreamingResourcesClosed() {
    StreamingStatistics stats = streamingManager.getStreamingStatistics();
    new PollingProber(10000, 100).check(new JUnitLambdaProbe(() -> {
      assertThat("There're still open cursor providers", stats.getOpenCursorProvidersCount(), is(0));
      assertThat("There're still open cursors", stats.getOpenCursorsCount(), is(0));
      return true;
    }));
  }

}
