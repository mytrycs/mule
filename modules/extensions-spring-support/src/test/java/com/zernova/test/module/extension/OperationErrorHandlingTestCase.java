/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static com.zernova.functional.api.exception.ExpectedError.none;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Unhandleable.FATAL;
import static com.zernova.runtime.core.api.error.Errors.Identifiers.CONNECTIVITY_ERROR_IDENTIFIER;
import static com.zernova.runtime.core.api.error.Errors.Identifiers.UNKNOWN_ERROR_IDENTIFIER;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.CORE_PREFIX;
import static com.zernova.tck.junit4.matcher.ErrorTypeMatcher.errorType;
import static com.zernova.test.heisenberg.extension.HeisenbergErrors.HEALTH;

import com.zernova.functional.api.exception.ExpectedError;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.exception.ZernovaFatalException;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.test.heisenberg.extension.exception.HeisenbergException;

import org.junit.Rule;
import org.junit.Test;

public class OperationErrorHandlingTestCase extends AbstractExtensionFunctionalTestCase {

  private static final String HEISENBERG = "HEISENBERG";

  @Rule
  public ExpectedError expectedError = none();

  @Override
  protected String getConfigFile() {
    return "heisenberg-operation-error-handling-config.xml";
  }

  @Test
  public void heisenbergThrowsAHealthErrorFromHeisenbergException() throws Exception {
    expectedError.expectErrorType(HEISENBERG, HEALTH.getType()).expectCause(instanceOf(HeisenbergException.class));
    flowRunner("cureCancer").run();
  }

  @Test
  public void heisenbergThrowsMessagingExceptionWithEventAndFailingProcessorPopulated() throws Exception {
    expectedError.expectEvent(notNullValue(CoreEvent.class));
    expectedError.expectFailingComponent(is(locator
        .find(Location.builder().globalName("cureCancer").addProcessorsPart().addIndexPart(0).build()).get()));
    flowRunner("cureCancer").run();
  }

  @Test
  public void connectionExceptionThrowsAnConnectivityError() throws Exception {
    expectedError.expectErrorType(HEISENBERG, CONNECTIVITY_ERROR_IDENTIFIER).expectCause(instanceOf(ConnectionException.class));
    flowRunner("connectionFails").run();
  }

  @Test
  public void unrecognizedExceptionIsUnknown() throws Exception {
    expectedError
        .expectErrorType(CORE_PREFIX.toUpperCase(), UNKNOWN_ERROR_IDENTIFIER)
        .expectCause(instanceOf(HeisenbergException.class));
    flowRunner("unrecognizedException").run();
  }

  @Test
  public void errorIsPropagatedCorrectly() throws Exception {
    expectedError
        .expectErrorType(FATAL.getNamespace().toUpperCase(), FATAL.getName())
        .expectCause(instanceOf(ZernovaFatalException.class));

    try {
      flowRunner("throwError").run();
      fail("Should've thrown an exception");

    } catch (Throwable t) {
      Throwable problem = t.getCause();
      assertThat(problem, instanceOf(ZernovaFatalException.class));
      assertThat(problem.getCause(), instanceOf(LinkageError.class));
      throw t;
    }
  }

  @Test
  public void customErrorsAreCaughtUsingWildcards() throws Exception {
    CoreEvent result = runFlow("errorCaughtUsingWildcard");
    assertThat(getPayloadAsString(result.getMessage()), is("Wildcard namespace was caught; Wildcard name was caught"));
  }

  @Test
  public void errorRaisedWithinScope() throws Exception {
    expectedError.expectErrorType("APP", "EXPECTED");
    runFlow("errorRaisedWithinScopeFlow");
  }
}
