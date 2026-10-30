/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.some.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.ANY;

import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;
import com.zernova.sdk.api.annotation.param.Optional;
import com.zernova.sdk.api.annotation.param.Parameter;

@MediaType(ANY)
public class ExclusiveOptionalsEmittingSource extends Source<Object, Void> {

  @ParameterGroup(name = "Parameter Group", showInDsl = true)
  SomeParameterGroupOneRequiredConfig someParameterGroup;

  @Parameter
  @Optional
  ComplexParameter pojoParameter;

  @Parameter
  @Optional
  String repeatedNameParameter;

  @Override
  public void onStart(SourceCallback<Object, Void> sourceCallback) {
    Pair<SomeParameterGroupOneRequiredConfig, ComplexParameter> parameters = new Pair<>(someParameterGroup, pojoParameter);
    sourceCallback.handle(Result.<Object, Void>builder().output(parameters).build());
  }

  @Override
  public void onStop() {}
}
