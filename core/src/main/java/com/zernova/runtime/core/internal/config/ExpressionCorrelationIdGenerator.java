/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.config;

import com.zernova.runtime.api.el.CompiledExpression;
import com.zernova.runtime.api.el.ExpressionLanguageSession;
import com.zernova.runtime.api.el.ExpressionCompilationException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.CorrelationIdGenerator;
import com.zernova.runtime.core.api.el.ExpressionManager;

import static java.lang.String.format;
import static com.zernova.runtime.api.el.BindingContextUtils.NULL_BINDING_CONTEXT;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

public class ExpressionCorrelationIdGenerator implements CorrelationIdGenerator {

  private String expression;

  private ZernovaContext context;

  private ExpressionManager manager;
  private CompiledExpression compiledExpression;

  public ExpressionCorrelationIdGenerator(ZernovaContext context, String expression) {
    this.expression = expression;
    this.context = context;
  }

  @Override
  public String generateCorrelationId() {
    try (ExpressionLanguageSession session = manager.openSession(NULL_BINDING_CONTEXT)) {
      return session.evaluate(compiledExpression).getValue().toString();
    }
  }

  private void validateExpression() {
    if (!manager.isExpression(expression) || !manager.isValid(expression)) {
      throw new ExpressionCompilationException(createStaticMessage(format("Invalid Correlation ID Generation expression: %s",
                                                                          expression)));
    }
  }

  // TODO (MULE-19231): remove this from here (won't be more necessary)
  public void initializeGenerator() {
    manager = context.getExpressionManager();
    compiledExpression = manager.compile(expression, NULL_BINDING_CONTEXT);
    validateExpression();
  }

}
