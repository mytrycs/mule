/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.report;

import static com.zernova.tck.report.ThreadDumper.logThreadDump;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.TestTimedOutException;
import org.slf4j.Logger;

import java.util.concurrent.TimeoutException;

/**
 * Rule that generates a thread dump on failure.
 *
 * @since 4.1
 */
public class ThreadDumpOnTimeOut extends TestWatcher {

  private static final Logger LOGGER = getLogger(ThreadDumpOnTimeOut.class);

  @Override
  protected void failed(Throwable e, Description description) {
    super.failed(e, description);

    if (e instanceof TestTimedOutException || e instanceof TimeoutException) {
      LOGGER.error("test timed out. Maybe due to a deadlock?");
      if (LOGGER.isTraceEnabled()) {
        logThreadDump();
      }
    }
  }
}
