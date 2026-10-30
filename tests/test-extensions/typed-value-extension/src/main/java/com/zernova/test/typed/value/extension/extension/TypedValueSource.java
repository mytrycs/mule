/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.typed.value.extension.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.execution.OnSuccess;
import com.zernova.runtime.extension.api.annotation.execution.OnTerminate;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;

@Alias("source")
@MediaType(TEXT_PLAIN)
public class TypedValueSource extends Source<String, Object> {

  public static TypedValue<String> onSuccessValue;

  @Override
  public void onStart(SourceCallback<String, Object> sourceCallback) throws ZernovaException {
    sourceCallback.handle(Result.<String, Object>builder().output("This is a string").build());
  }

  @Override
  public void onStop() {

  }

  @OnSuccess
  public void onSuccess(TypedValue<String> stringValue) {
    onSuccessValue = stringValue;
  }

  @OnTerminate
  public void onTerminate() {

  }
}
