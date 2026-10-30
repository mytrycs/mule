/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.execution;

import static java.lang.Thread.currentThread;
import static com.zernova.runtime.core.api.util.ClassUtils.setContextClassLoader;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutor.ExecutorCallback;

import java.util.Map;

import org.slf4j.MDC;

public class PreservingThreadContextExecutorCallback implements ExecutorCallback {

  private final ExecutorCallback delegate;
  private final ClassLoader classLoader;
  private final Map<String, String> mdc;

  public PreservingThreadContextExecutorCallback(ExecutorCallback delegate) {
    this.delegate = delegate;
    this.classLoader = currentThread().getContextClassLoader();
    this.mdc = MDC.getCopyOfContextMap();
  }

  @Override
  public void complete(Object value) {
    try (ThreadContext ignored = new ThreadContext(classLoader, mdc)) {
      delegate.complete(value);
    }
  }

  @Override
  public void error(Throwable e) {
    try (ThreadContext ignored = new ThreadContext(classLoader, mdc)) {
      delegate.error(e);
    }
  }

  private static class ThreadContext implements AutoCloseable {

    private final Thread currentThread;

    private final ClassLoader innerClassLoader;
    private final Map<String, String> innerMDC;

    private final ClassLoader outerClassLoader;
    private final Map<String, String> outerMDC;

    ThreadContext(ClassLoader classLoader, Map<String, String> mdc) {
      currentThread = currentThread();

      innerClassLoader = classLoader;
      innerMDC = mdc;

      outerClassLoader = currentThread.getContextClassLoader();
      outerMDC = MDC.getCopyOfContextMap();

      MDC.setContextMap(innerMDC);
      setContextClassLoader(currentThread, outerClassLoader, innerClassLoader);
    }

    @Override
    public void close() {
      try {
        setContextClassLoader(currentThread, innerClassLoader, outerClassLoader);
      } finally {
        MDC.setContextMap(outerMDC);
      }
    }
  }
}
