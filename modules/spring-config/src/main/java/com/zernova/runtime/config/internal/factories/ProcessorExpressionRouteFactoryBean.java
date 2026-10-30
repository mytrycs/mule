/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import com.zernova.runtime.core.internal.routing.ProcessorExpressionRoute;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;

public class ProcessorExpressionRouteFactoryBean extends AbstractProcessorRouteFactoryBean<ProcessorExpressionRoute> {

  private String expression;

  public void setExpression(String expression) {
    this.expression = expression;
  }

  @Override
  protected ProcessorExpressionRoute getProcessorRoute(MessageProcessorChain chain) {
    return new ProcessorExpressionRoute(expression, chain, componentTracerFactory);
  }
}
