/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import com.zernova.runtime.core.internal.security.PasswordBasedEncryptionStrategy;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.Test;

public class PbeEncryptionStrategyTestCase extends AbstractZernovaTestCase {

  @Test
  public void testRoundTripEncryption() throws Exception {
    PasswordBasedEncryptionStrategy pbe = new PasswordBasedEncryptionStrategy();
    pbe.setPassword("test");
    pbe.initialise();

    byte[] b = pbe.encrypt("hello".getBytes(), null);

    assertNotSame(new String(b), "hello");
    String s = new String(pbe.decrypt(b, null), "UTF-8");
    assertEquals("hello", s);
  }
}
