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

public class Deployment extends TypeSafeMatcher<ZernovaProcessController> {

  private final String applicationName;

  public Deployment(String applicationName) {
    this.applicationName = applicationName;
  }

  @Override
  public boolean matchesSafely(ZernovaProcessController mule) {
    return mule.isDeployed(applicationName);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("a Mule Standalone server that has successfully deployed: " + applicationName);
  }

  public static <T> Matcher<ZernovaProcessController> hasDeployed(String applicationName) {
    return new Deployment(applicationName);
  }

};
