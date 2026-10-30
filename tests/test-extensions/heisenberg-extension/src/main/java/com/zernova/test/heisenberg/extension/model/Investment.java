/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension.model;

import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.annotation.param.Optional;

import java.util.List;
import java.util.Map;

public abstract class Investment {

  private boolean approved = false;

  @Parameter
  private String commercialName;

  @Parameter
  @Optional
  private List<Investment> discardedInvestments;

  @Parameter
  @Optional
  private Map<String, Investment> investmentSpinOffs;

  @ParameterGroup(name = "Investment")
  private InvestmentInfo investmentInfo;

  public String getCommercialName() {
    return commercialName;
  }

  public InvestmentInfo getInvestmentInfo() {
    return investmentInfo;
  }

  public void approve() {
    approved = true;
  }

  public boolean isApproved() {
    return approved;
  }

  public List<Investment> getDiscardedInvestments() {
    return discardedInvestments;
  }

  public Map<String, Investment> getInvestmentSpinOffs() {
    return investmentSpinOffs;
  }
}
