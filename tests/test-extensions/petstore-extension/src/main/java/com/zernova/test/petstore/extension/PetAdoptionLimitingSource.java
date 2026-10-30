/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataScope;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.runtime.operation.Result;
import com.zernova.sdk.api.runtime.source.PollContext;
import com.zernova.sdk.api.runtime.source.PollingSource;
import com.zernova.sdk.api.runtime.source.SourceCallbackContext;

import java.util.List;

@MetadataScope(outputResolver = PollingSourceMetadataResolver.class)
@MediaType(TEXT_PLAIN)
public class PetAdoptionLimitingSource extends PollingSource<String, Integer> {

  private int pollCounter;

  @Parameter
  private List<String> pets;

  // Using this annotation in order to mix the legacy and the new api and test all code flows.
  @com.zernova.sdk.api.annotation.param.Parameter
  private List<Integer> watermarks;

  @Override
  protected void doStart() throws ZernovaException {
    resetCounters();
  }

  @Override
  protected void doStop() {
    resetCounters();
  }

  @Override
  public void poll(PollContext<String, Integer> pollContext) {
    for (int i = 0; i < pets.size(); i++) {
      final int index = i;
      pollContext.accept(item -> {
        item.setResult(Result.<String, Integer>builder().output(pets.get(index)).attributes(pollCounter).build());
        item.setId(pets.get(index));
        item.setWatermark(watermarks.get(index));
      });
    }
    pollCounter++;
  }

  @Override
  public void onRejectedItem(Result<String, Integer> result, SourceCallbackContext callbackContext) {

  }

  private synchronized void resetCounters() {
    pollCounter = 0;
  }
}
