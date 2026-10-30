/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension.model;

import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.Optional;

public class InvestmentInfo {

  @Parameter
  @Optional
  private Investment investmentPlanB;

  @Parameter
  private Long valuation;

  public Long getValuation() {
    return valuation;
  }

  public void setValuation(Long valuation) {
    this.valuation = valuation;
  }

  public Investment getInvestmentPlanB() {
    return investmentPlanB;
  }

  public void setInvestmentPlanB(Investment investmentPlanB) {
    this.investmentPlanB = investmentPlanB;
  }
}
