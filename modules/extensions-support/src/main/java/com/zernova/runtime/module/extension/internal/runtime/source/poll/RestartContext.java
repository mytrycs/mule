/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source.poll;

import com.zernova.runtime.api.scheduler.Scheduler;

/**
 * Context needed to perform the restart of a source
 *
 * @since 4.2.3 4.3.1 4.4.0
 */
public class RestartContext {

  private final Scheduler executor;
  private final DelegateRunnable delegateRunnable;

  public RestartContext(Scheduler executor, DelegateRunnable delegateRunnable) {
    this.executor = executor;
    this.delegateRunnable = delegateRunnable;
  }

  public Scheduler getExecutor() {
    return executor;
  }

  public DelegateRunnable getDelegateRunnable() {
    return delegateRunnable;
  }
}
