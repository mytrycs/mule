/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import static java.util.Comparator.naturalOrder;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.execution.OnError;
import com.zernova.runtime.extension.api.annotation.execution.OnSuccess;
import com.zernova.runtime.extension.api.annotation.execution.OnTerminate;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataScope;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.runtime.operation.Result;
import com.zernova.sdk.api.runtime.source.PollContext;
import com.zernova.sdk.api.runtime.source.PollingSource;
import com.zernova.sdk.api.runtime.source.SourceCallbackContext;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@MetadataScope(outputResolver = PollingSourceMetadataResolver.class)
@MediaType(TEXT_PLAIN)
public class WatermarkingPetAdoptionSource extends PollingSource<String, Integer> {

  private int pollCounter;
  private static int index = 0;
  private static boolean alreadyWaited = false;
  private static CountDownLatch continueLatch = new CountDownLatch(1);

  public static CountDownLatch beginLatch = new CountDownLatch(1);

  @Parameter
  @com.zernova.runtime.extension.api.annotation.param.Optional(defaultValue = "0")
  protected Integer awaitOnItem;

  // Using this annotation in order to mix the legacy and the new api and test all code flows.
  @com.zernova.sdk.api.annotation.param.Parameter
  protected List<String> pets;

  @Parameter
  protected List<Integer> watermarks;

  @Parameter
  protected Integer itemsPerPoll;

  @OnSuccess
  public synchronized void onSuccess() {}

  @OnError
  public synchronized void onError() {}

  @OnTerminate
  public synchronized void onTerminate() {}

  @Override
  protected void doStart() throws ZernovaException {}

  @Override
  protected void doStop() {}

  @Override
  public void poll(PollContext<String, Integer> pollContext) {
    for (int i = 0; i < itemsPerPoll && index < pets.size(); i++, index++) {
      if (index == (awaitOnItem - 1) && !alreadyWaited) {
        try {
          beginLatch.countDown();
          continueLatch.await();
        } catch (InterruptedException e) {
          // Stopping the source will interrupt the latch, so we do nothing and keep pushing items.
        } finally {
          alreadyWaited = true;
        }
      }
      pollContext.setWatermarkComparator(naturalOrder());
      Result<String, Integer> result = Result.<String, Integer>builder().output(pets.get(index)).attributes(pollCounter).build();
      Integer watermarkValue = watermarks.get(index);
      pollContext.accept(item -> {
        item.setResult(result);
        item.setId(result.getOutput().toLowerCase());
        item.setWatermark(watermarkValue);
      });
    }
    pollCounter++;
  }

  @Override
  public void onRejectedItem(Result<String, Integer> result, SourceCallbackContext context) {}

  public static synchronized void resetSource() {
    beginLatch = new CountDownLatch(1);
    continueLatch = new CountDownLatch(1);
    alreadyWaited = false;
    index = 0;
  }

}
