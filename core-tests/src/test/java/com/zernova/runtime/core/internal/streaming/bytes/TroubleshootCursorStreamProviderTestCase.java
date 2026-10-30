/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.streaming.bytes;

import static com.zernova.runtime.api.util.DataUnit.BYTE;
import static com.zernova.test.allure.AllureConstants.StreamingFeature.STREAMING;
import static com.zernova.test.allure.AllureConstants.StreamingFeature.StreamingStory.TROUBLESHOOTING;

import com.zernova.runtime.api.streaming.bytes.CursorStreamProvider;
import com.zernova.runtime.api.util.DataSize;
import com.zernova.runtime.core.api.streaming.bytes.InMemoryCursorStreamConfig;
import com.zernova.runtime.core.api.streaming.bytes.InMemoryCursorStreamProvider;
import com.zernova.runtime.core.internal.streaming.AbstractTroubleshootCursorProviderTestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.After;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(STREAMING)
@Story(TROUBLESHOOTING)
public class TroubleshootCursorStreamProviderTestCase extends AbstractTroubleshootCursorProviderTestCase {

  private static final byte[] DATA = "Hello".getBytes();

  private PoolingByteBufferManager bufferManager;

  @Override
  @After
  public void after() {
    super.after();
    bufferManager.dispose();
  }

  @Override
  protected CursorStreamProvider createCursorProvider() {
    int bufferSize = 10;
    int maxBufferSize = 20;
    InputStream dataStream = new ByteArrayInputStream(DATA);

    bufferManager = new PoolingByteBufferManager();

    InMemoryCursorStreamConfig config =
        new InMemoryCursorStreamConfig(new DataSize(bufferSize, BYTE),
                                       new DataSize(bufferSize / 2, BYTE),
                                       new DataSize(maxBufferSize, BYTE),
                                       false);

    return new InMemoryCursorStreamProvider(dataStream, config, bufferManager, componentLocation, trackStackTrace);
  }

}
