/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.marvel.xmen;

import static java.util.Arrays.asList;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.ANY;

import java.util.Iterator;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.execution.OnSuccess;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.parameter.CorrelationInfo;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;
import com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext;

@MediaType(ANY)
public class MagnetoBrotherhood extends Source<Iterator, Void> {

  @Override
  public void onStart(SourceCallback<Iterator, Void> sourceCallback) throws ZernovaException {
    sourceCallback.handle(makeResult());
  }

  private Result<Iterator, Void> makeResult() {
    return Result.<Iterator, Void>builder()
        .output(asList("QuickSilver", "Scarlet Witch", "Toad", "Magneto Ally", "Mystique", "Blob").iterator())
        .build();
  }

  @OnSuccess
  public void onSuccess(@ParameterGroup(name = "Response", showInDsl = true) MutantUnitedResponse mutantResponse,
                        CorrelationInfo correlationInfo) {
    if (mutantResponse.getBody().getValue() instanceof Iterator) {
      Iterator iterator = ((Iterator) mutantResponse.getBody().getValue());
      // Consuming the elements
      while (iterator.hasNext()) {
        iterator.next();
      }
    }
  }

  @Override
  public void onStop() {
    // Nothing to do
  }

}
