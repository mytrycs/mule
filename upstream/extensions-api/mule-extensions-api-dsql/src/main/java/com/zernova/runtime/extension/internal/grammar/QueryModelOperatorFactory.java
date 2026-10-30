/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.grammar;

import com.zernova.runtime.extension.internal.operator.EqualsOperator;
import com.zernova.runtime.extension.internal.operator.GreaterOperator;
import com.zernova.runtime.extension.internal.operator.GreaterOrEqualsOperator;
import com.zernova.runtime.extension.internal.operator.LessOperator;
import com.zernova.runtime.extension.internal.operator.LessOrEqualsOperator;
import com.zernova.runtime.extension.internal.operator.LikeOperator;
import com.zernova.runtime.extension.internal.operator.NotEqualsOperator;
import com.zernova.runtime.extension.internal.operator.Operator;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class that returns {@link Operator} expression instances depending on the token that is passed as parameter in the
 * {@link QueryModelOperatorFactory#getOperator(String)}
 *
 * @since 1.0
 */
final class QueryModelOperatorFactory {

  private static QueryModelOperatorFactory INSTANCE = new QueryModelOperatorFactory();
  private Map<String, Operator> operators = new HashMap<>();

  private QueryModelOperatorFactory() {
    operators.put("=", new EqualsOperator());
    operators.put(">", new GreaterOperator());
    operators.put("<", new LessOperator());
    operators.put(">=", new GreaterOrEqualsOperator());
    operators.put("<=", new LessOrEqualsOperator());
    operators.put("<>", new NotEqualsOperator());
    operators.put("like", new LikeOperator());
  }

  public static QueryModelOperatorFactory getInstance() {
    return INSTANCE;
  }

  Operator getOperator(String symbol) {
    return operators.get(symbol.toLowerCase().trim());
  }
}
