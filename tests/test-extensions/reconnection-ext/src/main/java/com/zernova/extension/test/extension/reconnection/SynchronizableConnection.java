/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.extension.test.extension.reconnection;


import com.zernova.runtime.api.util.concurrent.Latch;

/**
 * This class represents an extension connection just as example (there is no real connection with anything here c:).
 */
public final class SynchronizableConnection {

  private final Latch disconnectionLatch;
  public static boolean disconnectionWaitedFullTimeout = false;

  public SynchronizableConnection() {
    this.disconnectionLatch = new Latch();
  }

  public Latch getDisconnectionLatch() {
    return disconnectionLatch;
  }

  public void setDisconnectionWaitedFullTimeout(boolean value) {
    disconnectionWaitedFullTimeout = value;
  }
}
