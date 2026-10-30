/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.core.el;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import com.zernova.AbstractBenchmarkAssertionTestCase;
import com.zernova.el.ExpressionBenchmark;

import org.junit.Test;

public class ExpressionBenchmarkAssertionTestCase extends AbstractBenchmarkAssertionTestCase {

  private static final String EXPRESSION_PARAM = "expression";

  @Test
  public void evaluatePayloadMEL() {
    runAndAssertBenchmark(ExpressionBenchmark.class, "melPayload", 700, NANOSECONDS, 2000);
  }

  @Test
  public void evaluatePayloadDW() {
    // TODO MULE-11971 DW numbers are significantly below MEL currently but by adding assertions we avoid any regressions
    runAndAssertBenchmark(ExpressionBenchmark.class, "dwPayload", 5000, NANOSECONDS, 7000);
  }

}
