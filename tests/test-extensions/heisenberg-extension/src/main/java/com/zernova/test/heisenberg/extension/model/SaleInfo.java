/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension.model;

import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.test.heisenberg.extension.HeisenbergOperations;

import java.util.Map;

/**
 * DON'T USE THIS CLASS BEYOND {@link HeisenbergOperations#processSale(Map)} READ: MULE-10303
 */
public class SaleInfo {

  @Parameter
  private String details;

  @Parameter
  private Integer amount;

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public Integer getAmount() {
    return amount;
  }

  public void setAmount(Integer amount) {
    this.amount = amount;
  }
}
