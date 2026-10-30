/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.infrastructure.process.matchers;

import com.zernova.test.infrastructure.process.ZernovaProcessController;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class Running extends TypeSafeMatcher<ZernovaProcessController> {

  private final boolean isRunning;

  public Running(boolean status) {
    this.isRunning = status;
  }

  @Override
  public boolean matchesSafely(ZernovaProcessController mule) {
    try {
      return isRunning == mule.isRunning();
    } catch (Error e) {
      return false;
    }
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("a Mule Standalone server that is " + (isRunning ? "" : "not ") + "running");
  }

  public static <T> Matcher<ZernovaProcessController> isRunning() {
    return new Running(true);
  }

  public static <T> Matcher<ZernovaProcessController> notRunning() {
    return new Running(false);
  }

};
