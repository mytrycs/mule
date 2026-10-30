/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.operator;


import com.zernova.runtime.extension.api.dsql.DefaultOperatorTranslator;

/**
 * Base contract for all binary operators.
 *
 * @since 1.0
 */
abstract class AbstractBinaryOperator extends BaseOperator implements BinaryOperator {

  @Override
  public String toString() {
    return accept(new DefaultOperatorTranslator());
  }
}
