/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.test.internal;

import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.util.concurrent.Latch;

public class WaitComponent implements Initialisable {

  public static Latch componentInitializedLatch = new Latch();
  public static Latch waitLatch = new Latch();

  @Override
  public void initialise() throws InitialisationException {
    try {
      componentInitializedLatch.release();
      waitLatch.await();
    } catch (InterruptedException e) {
      throw new InitialisationException(e, this);
    }
  }

  public static void reset() {
    componentInitializedLatch = new Latch();
    waitLatch = new Latch();
  }
}
