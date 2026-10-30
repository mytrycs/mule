/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.internal.util.extension.connectivity.basic;

import com.zernova.runtime.extension.api.annotation.Expression;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.api.meta.ExpressionSupport;

import java.util.Objects;

public class Account {

  @Parameter
  @Expression(ExpressionSupport.REQUIRED)
  private Owner requiredInnerPojoWithExpressionRequired;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Account account = (Account) o;
    return Objects.equals(requiredInnerPojoWithExpressionRequired, account.requiredInnerPojoWithExpressionRequired);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requiredInnerPojoWithExpressionRequired);
  }
}
