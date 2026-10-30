/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.dsql;

import static org.junit.Assert.assertEquals;
import com.zernova.runtime.extension.internal.operator.EqualsOperator;
import com.zernova.runtime.extension.internal.operator.GreaterOperator;
import com.zernova.runtime.extension.internal.operator.GreaterOrEqualsOperator;
import com.zernova.runtime.extension.internal.operator.LessOperator;
import com.zernova.runtime.extension.internal.operator.LessOrEqualsOperator;
import com.zernova.runtime.extension.internal.operator.LikeOperator;
import com.zernova.runtime.extension.internal.operator.NotEqualsOperator;

import org.junit.Test;


public class AbstractBinaryOperatorTestCase {

  @Test
  public void testOperationsSymbols() {
    assertEquals(" < ", (new LessOperator()).toString());
    assertEquals(" <= ", (new LessOrEqualsOperator()).toString());
    assertEquals(" = ", (new EqualsOperator()).toString());
    assertEquals(" > ", (new GreaterOperator()).toString());
    assertEquals(" >= ", (new GreaterOrEqualsOperator()).toString());
    assertEquals(" <> ", (new NotEqualsOperator()).toString());
    assertEquals(" like ", (new LikeOperator()).toString());
  }
}
