/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.infrastructure.process;

import java.io.File;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class VerifyLogWithMatcher extends TypeSafeMatcher<ZernovaProcessController> {

  private String app;
  private final Matcher<File> fileMatcher;

  public static Matcher<ZernovaProcessController> log(Matcher<File> matcher) {
    return new VerifyLogWithMatcher(matcher);
  }

  public static Matcher<ZernovaProcessController> log(String appName, Matcher<File> matcher) {
    return new VerifyLogWithMatcher(appName, matcher);
  }

  private VerifyLogWithMatcher(Matcher<File> matcher) {
    fileMatcher = matcher;
  }

  private VerifyLogWithMatcher(String appName, Matcher<File> matcher) {
    this.app = appName;
    fileMatcher = matcher;
  }

  @Override
  public boolean matchesSafely(ZernovaProcessController muleProcessController) {
    File logFile = (app == null) ? muleProcessController.getLog() : muleProcessController.getLog(app);
    return fileMatcher.matches(logFile);
  }


  @Override
  public void describeTo(Description description) {
    String message = (app == null) ? "mule log that has " : String.format("%s application log that have ", app);
    description.appendText(message).appendDescriptionOf(fileMatcher);
  }
}
