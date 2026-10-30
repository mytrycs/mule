/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.test.dsl.model;

import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.Extensible;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

import java.util.List;

@Extensible
@Alias("aliasedExtensible")
public class ExtensibleType {

  public static final String ALIAS = "aliasedExtensible";

  @Parameter
  private String sampleString;

  @Parameter
  private Integer otherNumber;

  @Parameter
  private List<Integer> childNumbers;

  public List<Integer> getChildNumbers() {
    return childNumbers;
  }

  public String getSampleString() {
    return sampleString;
  }

  public void setSampleString(String sampleString) {
    this.sampleString = sampleString;
  }

  public Integer getOtherNumber() {
    return otherNumber;
  }

  public void setOtherNumber(Integer otherNumber) {
    this.otherNumber = otherNumber;
  }
}
