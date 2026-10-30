/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;

import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.extension.api.annotation.param.Content;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

public class VeganProductInformation {

  @Parameter
  @Content(primary = true)
  private TypedValue<String> description;

  @Parameter
  @Optional
  @Content
  private TypedValue<String> brandName;

  @Optional
  @Parameter
  private TypedValue<Integer> weight;

  public TypedValue<String> getDescription() {
    return description;
  }

  public void setDescription(TypedValue<String> description) {
    this.description = description;
  }

  public TypedValue<String> getBrandName() {
    return brandName;
  }

  public void setBrandName(TypedValue<String> brandName) {
    this.brandName = brandName;
  }

  public TypedValue<Integer> getWeight() {
    return weight;
  }

  public void setWeight(TypedValue<Integer> weight) {
    this.weight = weight;
  }
}
