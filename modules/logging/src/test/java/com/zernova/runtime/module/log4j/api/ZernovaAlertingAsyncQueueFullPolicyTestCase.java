/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.log4j.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

public class ZernovaAlertingAsyncQueueFullPolicyTestCase {

  @Test
  public void callbackRegistration() {
    var policy = new ZernovaAlertingAsyncQueueFullPolicy();

    final var callback = mock(Consumer.class);
    policy.register(this.getClass().getClassLoader(), callback);

    policy.getRoute(1, Level.INFO);
    verify(callback).accept(this.getClass().getClassLoader());
  }
}
