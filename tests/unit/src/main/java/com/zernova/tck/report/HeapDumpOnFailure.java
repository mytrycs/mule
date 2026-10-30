/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.report;

import static com.zernova.tck.report.HeapDumper.dumpHeap;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Rule that generates a heap dump on failure.
 * <p>
 * Useful for troubleshooting failures in memory leak prevention tests.
 */
public class HeapDumpOnFailure extends TestWatcher {

  @Override
  protected void failed(Throwable e, Description description) {
    super.failed(e, description);
    dumpHeap(description.getTestClass().getName() + description.getMethodName() + ".hprof", true);
  }
}
