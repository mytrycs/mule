/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.test.exception;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaExceptionInfo;
import com.zernova.runtime.api.message.ErrorType;

import io.qameta.allure.Issue;
import org.junit.Before;
import org.junit.Test;

public class ZernovaExceptionInfoTestCase {

  ZernovaExceptionInfo muleExceptionInfo = null;
  private final ErrorType errorType = mock(ErrorType.class);

  @Before
  public void before() {
    when(errorType.toString()).thenReturn("ZERNOVA:TESTING");
    muleExceptionInfo = new ZernovaExceptionInfo();
  }

  @Test
  @Issue("MULE-18041")
  public void whenCausedByExistsThenIsAddedToSummary() {
    StringBuilder summaryMessage = new StringBuilder();
    muleExceptionInfo.addSuppressedCause(new TestZernovaException("Caused by error"));
    muleExceptionInfo.addToSummaryMessage(summaryMessage);
    assertThat(summaryMessage.toString(), containsString("Caused by             : Caused by error"));
  }

  @Test
  @Issue("MULE-18041")
  public void whenCausedByErrorTypeExistsThenIsAddedToSummary() {
    StringBuilder summaryMessage = new StringBuilder();
    muleExceptionInfo.addSuppressedCause(new TestZernovaException("Caused by error", errorType));
    muleExceptionInfo.addToSummaryMessage(summaryMessage);
    assertThat(summaryMessage.toString(), containsString("Caused by             : ZERNOVA:TESTING: Caused by error"));
  }

  @Test
  @Issue("MULE-18041")
  public void whenCausedByNotExistsThenIsNotAddedToSummary() {
    StringBuilder summaryMessage = new StringBuilder();
    muleExceptionInfo.addToSummaryMessage(summaryMessage);
    assertThat(summaryMessage.toString(), not(containsString("Caused by error")));
  }

  private static class TestZernovaException extends ZernovaException {

    private static final long serialVersionUID = -8941618124275385459L;

    public TestZernovaException(String message) {
      super(createStaticMessage(message));
    }

    public TestZernovaException(String message, ErrorType errorType) {
      super(createStaticMessage(message));
      getExceptionInfo().setErrorType(errorType);
    }
  }
}
