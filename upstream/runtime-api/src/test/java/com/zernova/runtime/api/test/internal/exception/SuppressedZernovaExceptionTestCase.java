/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.test.internal.exception;

import static java.lang.Boolean.parseBoolean;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;
import static com.zernova.runtime.api.exception.ZernovaException.MULE_VERBOSE_EXCEPTIONS;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.api.i18n.I18nMessageFactory;
import com.zernova.runtime.privileged.exception.SuppressedZernovaException;

import java.util.List;
import java.util.Map;

import io.qameta.allure.Issue;
import org.junit.Test;

@RunWith(Parameterized.class)
public class SuppressedZernovaExceptionTestCase {

  private final boolean isVerboseExceptions;
  private boolean wasVerboseExceptions;

  @Parameterized.Parameters(name = "Verbose exceptions: {0}")
  public static List<Object[]> parameters() {
    return asList(
                  new Object[] {true},
                  new Object[] {false});
  }

  public SuppressedZernovaExceptionTestCase(boolean isVerboseExceptions) {
    this.isVerboseExceptions = isVerboseExceptions;
  }

  @Before
  public void setUp() throws Exception {
    this.wasVerboseExceptions = parseBoolean(System.clearProperty(MULE_VERBOSE_EXCEPTIONS));
    System.setProperty(MULE_VERBOSE_EXCEPTIONS, Boolean.toString(isVerboseExceptions));
    ZernovaException.refreshVerboseExceptions();
  }

  @After
  public void tearDown() throws Exception {
    System.setProperty(MULE_VERBOSE_EXCEPTIONS, Boolean.toString(wasVerboseExceptions));
  }

  @Test
  @Issue("MULE-18041")
  public void whenClassToSuppressIsNotFoundThenNoSuppressionIsAdded() {
    Throwable result =
        SuppressedZernovaException.suppressIfPresent(new SimpleException(I18nMessageFactory.createStaticMessage("Test")),
                                                  ExceptionWithAdditionalInfo.class);
    assertThat(result, is(instanceOf(SimpleException.class)));
  }

  @Test
  @Issue("MULE-18041")
  public void whenClassToSuppressIsFoundThenSuppressionIsAdded() {
    Throwable result =
        SuppressedZernovaException.suppressIfPresent(new SimpleException(new SimpleException(new ExceptionWithAdditionalInfo())),
                                                  ExceptionWithAdditionalInfo.class);
    assertThat(result, is(instanceOf(SuppressedZernovaException.class)));
  }

  @Test
  @Issue("MULE-18041")
  public void whenSuppressionIsAddedThenIncludeSuppressedAdditionalProperties() {
    Throwable suppressedTestException =
        SuppressedZernovaException.suppressIfPresent(new SimpleException(new ExceptionWithAdditionalInfo()), SimpleException.class);
    Map<String, Object> suppressedTestExceptionEntries = ((SuppressedZernovaException) suppressedTestException).getInfo();
    Throwable suppressedAnotherTestException =
        SuppressedZernovaException.suppressIfPresent(new SimpleException(new ExceptionWithAdditionalInfo()),
                                                  ExceptionWithAdditionalInfo.class);
    Map<String, Object> suppressedAnotherTestExceptionEntries =
        ((SuppressedZernovaException) suppressedAnotherTestException).getInfo();
    assertThat(suppressedTestExceptionEntries, hasEntry("Additional entry key", "Test additional entry value"));
    assertThat(suppressedAnotherTestExceptionEntries, hasEntry("Additional entry key", "Test additional entry value"));
  }

  @Test
  @Issue("MULE-18041")
  public void whenSuppressionIsAddedThenPreviousSuppressionsAreAdded() {
    Throwable suppressedTestException = SuppressedZernovaException
        .suppressIfPresent(new SimpleException(I18nMessageFactory.createStaticMessage("Test")), SimpleException.class);
    Throwable suppressedAnotherTestException =
        SuppressedZernovaException.suppressIfPresent(new ExceptionWithAdditionalInfo(suppressedTestException),
                                                  ExceptionWithAdditionalInfo.class);
    List<ZernovaException> suppressions = ((ZernovaException) suppressedAnotherTestException).getExceptionInfo().getSuppressedCauses();
    assertThat(suppressions,
               contains(sameInstance(((SuppressedZernovaException) suppressedAnotherTestException).getSuppressedException()),
                        sameInstance(((SuppressedZernovaException) suppressedTestException).getSuppressedException())));
  }

  @Test
  public void selfCausedExceptionInMainLoopMustBeResolved() {
    Throwable suppressedSelfCausedException =
        SuppressedZernovaException.suppressIfPresent(new SimpleException(new SelfCausedException()), SelfCausedException.class);
    assertThat(suppressedSelfCausedException, is(instanceOf(SuppressedZernovaException.class)));
    List<ZernovaException> suppressions = ((ZernovaException) suppressedSelfCausedException).getExceptionInfo().getSuppressedCauses();
    assertThat(suppressions,
               contains(sameInstance(((SuppressedZernovaException) suppressedSelfCausedException).getSuppressedException())));
  }

  @Test
  public void selfCausedExceptionInSecondaryLoopMustBeResolved() {
    Throwable suppressedSelfCausedException =
        SuppressedZernovaException.suppressIfPresent(new SimpleException(new SelfCausedException()), SimpleException.class);
    assertThat(suppressedSelfCausedException, is(instanceOf(SuppressedZernovaException.class)));
    List<ZernovaException> suppressions = ((ZernovaException) suppressedSelfCausedException).getExceptionInfo().getSuppressedCauses();
    assertThat(suppressions,
               contains(sameInstance(((SuppressedZernovaException) suppressedSelfCausedException).getSuppressedException())));
  }

  @Test
  @Issue("MULE-18562")
  public void consecutiveSuppressionsMustNotBePossible() {
    SimpleException exception = new SimpleException(new ExceptionWithAdditionalInfo());
    SuppressedZernovaException suppression =
        (SuppressedZernovaException) SuppressedZernovaException.suppressIfPresent(exception,
                                                                            ExceptionWithAdditionalInfo.class);
    SuppressedZernovaException attemptedConsecutiveSuppression =
        (SuppressedZernovaException) SuppressedZernovaException.suppressIfPresent(suppression,
                                                                            SimpleException.class);
    assertThat(attemptedConsecutiveSuppression, is(suppression));
  }

  @Test
  @Issue("MULE-18562")
  public void suppressionMustBeUnwrapped() {
    SimpleException unwrappedException = new SimpleException();
    SuppressedZernovaException result =
        (SuppressedZernovaException) SuppressedZernovaException.suppressIfPresent(unwrappedException,
                                                                            SimpleException.class);
    assertThat(result.unwrap(), is(unwrappedException));
  }

  @Test
  @Issue("W-15643200")
  public void suppressionMustHaveItsOwnDetailedMessage() {
    SimpleException unwrappedException = new SimpleException(I18nMessageFactory.createStaticMessage("Test"));
    SuppressedZernovaException result =
        (SuppressedZernovaException) SuppressedZernovaException
            .suppressIfPresent(new SimpleException(I18nMessageFactory.createStaticMessage("Test")),
                               SimpleException.class);
    assertThat(result.getDetailedMessage(), equalTo("Suppressed: " + unwrappedException.getMessage()));
  }

  private static class SimpleException extends ZernovaException {

    private static final long serialVersionUID = 21078091124109763L;

    public SimpleException(I18nMessage test) {
      super(test);
    }

    public SimpleException(Throwable cause) {
      super(cause);
    }

    public SimpleException() {
      super();
    }
  }

  private static class ExceptionWithAdditionalInfo extends ZernovaException {

    private static final long serialVersionUID = -2095590887184779909L;

    public ExceptionWithAdditionalInfo() {
      getExceptionInfo().putAdditionalEntry("Additional entry key", "Test additional entry value");
    }

    public ExceptionWithAdditionalInfo(Throwable cause) {
      super(cause);
      getExceptionInfo().putAdditionalEntry("Additional entry key", "Test additional entry value");
    }

  }

  private static class SelfCausedException extends ZernovaException {

    private static final long serialVersionUID = -8420615463020752243L;

    @Override
    public synchronized Throwable getCause() {
      return this;
    }

  }
}
