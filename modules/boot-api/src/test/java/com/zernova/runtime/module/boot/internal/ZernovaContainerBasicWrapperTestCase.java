/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.boot.internal;

import static org.junit.rules.ExpectedException.none;

import com.zernova.tck.size.SmallTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.qameta.allure.Issue;

@SmallTest
public class ZernovaContainerBasicWrapperTestCase {

  @Rule
  public ExpectedException expected = none();

  @Test
  @Issue("W-14742344")
  public void haltAndCatchFireNoError() {
    new ZernovaContainerBasicWrapper().haltAndCatchFire(0, null);
    // no exception thrown
  }

  @Test
  public void haltAndCatchFireError() {
    final String errorMessage = "error message";
    expected.expectMessage(errorMessage);
    new ZernovaContainerBasicWrapper().haltAndCatchFire(-1, errorMessage);
  }
}
