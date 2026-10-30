/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.data.sample.extension;


import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

public class SampleDataAliasedParameterGroup {

  @Parameter
  @Alias("aliasedPayload")
  private String payload;

  @Parameter
  @Optional
  @Alias("aliasedAttributes")
  private String attributes;

  public String getPayload() {
    return payload;
  }

  public String getAttributes() {
    return attributes;
  }
}
