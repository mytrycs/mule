/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.marvel.xmen;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;

import java.util.ArrayList;
import java.util.List;

@MediaType(TEXT_PLAIN)
public class CerebroDetectNewMutants extends Source<List<Result<String, Void>>, Void> {

  @Override
  public void onStart(SourceCallback<List<Result<String, Void>>, Void> sourceCallback) throws ZernovaException {
    sourceCallback.handle(makeResult());
  }

  private Result<List<Result<String, Void>>, Void> makeResult() {
    List<Result<String, Void>> mutants = new ArrayList<>();
    mutants.add(Result.<String, Void>builder()
        .output("Banshee")
        .build());
    mutants.add(Result.<String, Void>builder()
        .output("Jubilee")
        .build());
    mutants.add(Result.<String, Void>builder()
        .output("Blink")
        .build());

    return Result.<List<Result<String, Void>>, Void>builder()
        .output(mutants)
        .build();
  }

  @Override
  public void onStop() {
    // Nothing to do
  }

}
