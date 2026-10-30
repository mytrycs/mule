/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.nonimplicit.config.extension.extension.api;

import com.zernova.runtime.extension.api.annotation.param.NullSafe;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

public class NullSafeParameterGroup {

  @Parameter
  @Optional
  @NullSafe
  private NullSafePojo nullSafePojo;

  public NullSafePojo getNullSafePojo() {
    return nullSafePojo;
  }

  public void setNullSafePojo(NullSafePojo nullSafePojo) {
    this.nullSafePojo = nullSafePojo;
  }
}
