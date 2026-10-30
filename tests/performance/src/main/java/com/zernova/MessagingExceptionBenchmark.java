/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova;

import static com.zernova.runtime.core.api.config.i18n.CoreMessages.versionNotSet;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.DefaultZernovaException;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Threads;

public class MessagingExceptionBenchmark extends AbstractBenchmark {

  @Benchmark
  @Threads(1)
  public ZernovaException stringSingleThread() {
    return new DefaultZernovaException("customMessage");
  }

  @Benchmark
  @Threads(1)
  public ZernovaException messageSingleThead() {
    return new DefaultZernovaException(versionNotSet());
  }

  @Benchmark
  @Threads(4)
  public ZernovaException messageMultiThread() {
    return new DefaultZernovaException(versionNotSet());
  }

  @Benchmark
  @Threads(4)
  public ZernovaException stringMultiThread() {
    return new DefaultZernovaException("customMessage");
  }

}
