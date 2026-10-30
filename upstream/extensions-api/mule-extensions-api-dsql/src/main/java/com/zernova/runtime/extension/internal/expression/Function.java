/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.expression;



import com.zernova.runtime.extension.api.dsql.Expression;
import com.zernova.runtime.extension.api.dsql.QueryTranslator;

/**
 * Represents query expressions which contains functions.
 * <p>
 * E.g.: ... WHERE MAX(field) ...
 *
 * @since 1.0
 */
public class Function extends Expression {

  /**
   * Function expression
   */
  private String function;

  public String getFunction() {
    return function;
  }

  public void setFunction(String function) {
    this.function = function;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(QueryTranslator queryVisitor) {
    // TODO
  }
}
