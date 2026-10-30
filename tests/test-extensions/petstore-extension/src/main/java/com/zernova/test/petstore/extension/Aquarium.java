/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;

import java.util.Objects;

public class Aquarium {

  @com.zernova.sdk.api.annotation.param.ParameterGroup(name = "pond")
  public Pond pond;

  @Parameter
  @Optional(defaultValue = "50")
  public Integer ticketPrice;

  public Integer getTicketPrice() {
    return ticketPrice;
  }

  public Pond getPond() {
    return pond;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Aquarium aquarium = (Aquarium) o;
    return Objects.equals(pond, aquarium.pond) &&
        Objects.equals(ticketPrice, aquarium.ticketPrice);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pond, ticketPrice);
  }
}
