/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.junit4.rule;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Defines a {@link TestRule} that checks for timeouts in the execution of the tests, but differently from the JUnit's
 * {@link org.junit.rules.Timeout} class, just prints a warning in the log and the test still pass.
 * <p>
 * This was implemented in order to maintain the old "failOnTimeout=false" feature from
 * {@link com.zernova.tck.junit4.AbstractZernovaTestCase}
 */
public class WarningTimeout implements TestRule {

  private final int milliseconds;

  public WarningTimeout(int milliseconds) {
    this.milliseconds = milliseconds;
  }

  public Statement apply(Statement statement, Description description) {
    return new WarnOnTimeout(statement, milliseconds);
  }
}
