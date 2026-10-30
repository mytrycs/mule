/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.test.exception;

import io.qameta.allure.Issue;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static com.zernova.runtime.internal.exception.SuppressedZernovaException.suppressIfPresent;

import com.zernova.runtime.api.exception.DefaultZernovaException;
import com.zernova.runtime.api.exception.ZernovaException;

public class ZernovaExceptionTestCase {

  @Test
  @Issue("MULE-18041")
  public void summaryMessageMustNotIncludeAllInformation() {
    TestException testException = new TestException();
    assertThat(testException.getSummaryMessage(), not(containsString("Additional info value")));
  }

  @Test
  @Issue("MULE-18041")
  public void verboseMessageMustIncludeAllInformation() {
    TestException testException = new TestException();
    assertThat(testException.getVerboseMessage(), containsString("Additional info value"));
  }

  @Test
  @Issue("MULE-19811")
  public void summaryMessageWithNullRootMuleException() {
    ZernovaException muleException =
        (ZernovaException) suppressIfPresent(new DefaultZernovaException("Suppressed exception", new TestException()),
                                          DefaultZernovaException.class);
    assertThat(muleException.getSummaryMessage(), containsString("Suppressed exception"));
  }

  private static class TestException extends ZernovaException {

    private static final long serialVersionUID = 1029575542191754519L;

    public TestException() {
      addInfo("Additional info key", "Additional info value");
    }
  }
}
