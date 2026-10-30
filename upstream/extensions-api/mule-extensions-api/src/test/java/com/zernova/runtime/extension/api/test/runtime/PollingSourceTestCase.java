/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.test.runtime;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static com.zernova.runtime.extension.api.runtime.source.PollingSource.IDS_ON_UPDATED_WATERMARK_OS_NAME_SUFFIX;
import static com.zernova.runtime.extension.api.runtime.source.PollingSource.OS_NAME_MASK;
import static com.zernova.runtime.extension.api.runtime.source.PollingSource.RECENTLY_PROCESSED_IDS_OS_NAME_SUFFIX;
import static com.zernova.runtime.extension.api.runtime.source.PollingSource.WATERMARK_OS_NAME_SUFFIX;

import org.junit.Test;

public class PollingSourceTestCase {

  public static final String TEST_FLOW_NAME = "myFlow";
  public static final String EXPECTED_WATERMARK_OS = "_pollingSource_myFlow/watermark";
  public static final String EXPECTED_RECENT_IDS_OS = "_pollingSource_myFlow/recently-processed-ids";
  public static final String EXPECTED_IDS_UPDATED_WATERMARK_OS = "_pollingSource_myFlow/ids-on-updated-watermark";

  @Test
  public void watermarkObjectStoreNameIsBackwardsCompatible() {
    final String watermarkOs = format(OS_NAME_MASK, TEST_FLOW_NAME, WATERMARK_OS_NAME_SUFFIX);
    assertThat(watermarkOs, is(EXPECTED_WATERMARK_OS));
  }

  @Test
  public void recentlyProcessedIdsObjectStoreNameIsBackwardsCompatible() {
    final String watermarkOs = format(OS_NAME_MASK, TEST_FLOW_NAME, RECENTLY_PROCESSED_IDS_OS_NAME_SUFFIX);
    assertThat(watermarkOs, is(EXPECTED_RECENT_IDS_OS));
  }

  @Test
  public void idsUpdatedWatermarkObjectStoreNameIsBackwardsCompatible() {
    final String watermarkOs = format(OS_NAME_MASK, TEST_FLOW_NAME, IDS_ON_UPDATED_WATERMARK_OS_NAME_SUFFIX);
    assertThat(watermarkOs, is(EXPECTED_IDS_UPDATED_WATERMARK_OS));
  }
}
