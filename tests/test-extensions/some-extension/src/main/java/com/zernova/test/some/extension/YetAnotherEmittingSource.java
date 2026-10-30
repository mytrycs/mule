/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.some.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.ANY;

import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;

@MediaType(ANY)
public class YetAnotherEmittingSource extends Source<Object, Void> {

  @ParameterGroup(name = "Awesome Parameter Group", showInDsl = true)
  SomeParameterGroupOneRequiredConfig someParameterGroup;

  @Override
  public void onStart(SourceCallback<Object, Void> sourceCallback) {
    Object parameterValue = someParameterGroup.getSomeParameter() != null ? someParameterGroup.getSomeParameter()
        : someParameterGroup.getComplexParameter();
    sourceCallback.handle(Result.<Object, Void>builder().output(parameterValue).build());
  }

  @Override
  public void onStop() {}
}
