/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension.model.shops;

import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;

import java.util.Objects;

public class Order {

  @Parameter
  private int price;

  @ParameterGroup(name = "dessert")
  private Dessert dessert;

  public Dessert getDessert() {
    return dessert;
  }

  public int getPrice() {
    return price;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Order order = (Order) o;
    return price == order.price &&
        Objects.equals(dessert, order.dessert);
  }

  @Override
  public int hashCode() {
    return Objects.hash(price, dessert);
  }
}
