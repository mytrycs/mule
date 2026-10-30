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
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;

@MediaType(ANY)
public class AnotherExclusiveOptionalsEmittingSource extends Source<Object, Void> {

  @ParameterGroup(name = "Alias Parameter Group", showInDsl = true)
  SomeAliasedParameterGroupOneRequiredConfig someAliasedParameterGroup;

  @ParameterGroup(name = "Parameter Group")
  SomeParameterGroupOneRequiredConfig someParameterGroup;

  @Parameter
  @Optional
  @Alias(value = "some-parameter-alias")
  private String aliasedSomeParameter;

  @Override
  public void onStart(SourceCallback<Object, Void> sourceCallback) {
    Pair<SomeAliasedParameterGroupOneRequiredConfig, ComplexParameter> parameters = new Pair<>(someAliasedParameterGroup,
                                                                                               someParameterGroup != null
                                                                                                   ? someParameterGroup
                                                                                                       .getComplexParameter()
                                                                                                   : null);
    sourceCallback.handle(Result.<Object, Void>builder().output(parameters).build());
  }

  @Override
  public void onStop() {}
}
