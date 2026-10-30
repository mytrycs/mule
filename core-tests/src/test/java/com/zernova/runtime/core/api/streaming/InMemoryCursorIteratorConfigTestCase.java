/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.streaming;

import static com.zernova.test.allure.AllureConstants.StreamingFeature.STREAMING;
import static com.zernova.test.allure.AllureConstants.StreamingFeature.StreamingStory.BYTES_STREAMING;
import com.zernova.runtime.core.api.streaming.object.InMemoryCursorIteratorConfig;
import com.zernova.tck.size.SmallTest;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@SmallTest
@Feature(STREAMING)
@Story(BYTES_STREAMING)
public class InMemoryCursorIteratorConfigTestCase extends InMemoryStreamingConfigContractTestCase {

  @Override
  protected void createConfig(int initialSize, int increment, int maxSize) {
    new InMemoryCursorIteratorConfig(initialSize, increment, maxSize);
  }
}
