/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.classloading.internal;

import static com.zernova.test.classloading.api.ClassLoadingHelper.addClassLoader;

import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

public class AllOptionalParameterGroup {

  public static final String ALL_OPTIONAL_PARAMETER_GROUP = "allOptionalParameterGroup";

  @Parameter
  @Optional
  private String optionalString;

  @Parameter
  @Optional
  private Integer optionalInt;

  public AllOptionalParameterGroup() {
    addClassLoader(ALL_OPTIONAL_PARAMETER_GROUP);
  }

  public String getOptionalString() {
    return optionalString;
  }

  public Integer getOptionalInt() {
    return optionalInt;
  }
}
