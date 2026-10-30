/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.lock;

import com.zernova.runtime.api.lock.LockProvider;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link LockProvider} implementation for applications running in a single mule server
 */
public class SingleServerLockProvider implements LockProvider {

  @Override
  public Lock createLock(String lockId) {
    return new ReentrantLock(true);
  }

}
