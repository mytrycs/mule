/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.streaming.object;

import static java.util.Arrays.asList;
import static com.zernova.test.allure.AllureConstants.StreamingFeature.STREAMING;
import static com.zernova.test.allure.AllureConstants.StreamingFeature.StreamingStory.TROUBLESHOOTING;

import java.util.List;

import com.zernova.runtime.api.streaming.CursorProvider;
import com.zernova.runtime.core.api.streaming.object.InMemoryCursorIteratorConfig;
import com.zernova.runtime.core.internal.streaming.AbstractTroubleshootCursorProviderTestCase;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(STREAMING)
@Story(TROUBLESHOOTING)
public class TroubleshootCursorIteratorProviderTestCase extends AbstractTroubleshootCursorProviderTestCase {

  private static final List<String> DATA = asList("1", "2");

  protected CursorProvider createCursorProvider() {

    InMemoryCursorIteratorConfig config = new InMemoryCursorIteratorConfig(1, 1, 10);

    return new InMemoryCursorIteratorProvider(DATA.iterator(), config, componentLocation, trackStackTrace);

  }

}
