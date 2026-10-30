/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension.model;

import static com.zernova.runtime.extension.api.annotation.param.Optional.PAYLOAD;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.display.Placement;

public class KillParameters {


  public KillParameters() {}

  public KillParameters(String victim, String goodbyeMessage) {
    this.victim = victim;
    this.goodbyeMessage = goodbyeMessage;
  }

  @Parameter
  @Optional(defaultValue = PAYLOAD)
  @Placement(order = 1)
  private String victim;

  @Parameter
  @Placement(order = 2)
  private String goodbyeMessage;

  public String getVictim() {
    return victim;
  }

  public String getGoodbyeMessage() {
    return goodbyeMessage;
  }
}
