/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.exception;

import static com.zernova.runtime.ast.internal.error.DefaultErrorTypeBuilder.builder;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.ANY;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.CLIENT_SECURITY;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.COMPOSITE_ROUTING;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.CONNECTIVITY;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.DUPLICATE_MESSAGE;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.EXPRESSION;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.NOT_PERMITTED;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.REDELIVERY_EXHAUSTED;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.RETRY_EXHAUSTED;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.ROUTING;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.SECURITY;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.SERVER_SECURITY;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.STREAM_MAXIMUM_SIZE_EXCEEDED;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.TRANSACTION;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.TRANSFORMATION;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.UNKNOWN;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.VALIDATION;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Unhandleable.FATAL;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Unhandleable.FLOW_BACK_PRESSURE;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Unhandleable.OVERLOAD;
import static com.zernova.runtime.core.api.error.Errors.CORE_NAMESPACE_NAME;
import static com.zernova.runtime.core.api.error.Errors.Identifiers.TRANSACTION_ERROR_IDENTIFIER;
import static com.zernova.runtime.core.internal.exception.ErrorTypeLocatorFactory.createDefaultErrorTypeLocator;
import static com.zernova.test.allure.AllureConstants.ErrorHandlingFeature.ERROR_HANDLING;
import static com.zernova.test.allure.AllureConstants.ErrorHandlingFeature.ErrorHandlingStory.EXCEPTION_MAPPINGS;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import io.qameta.allure.Issue;
import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.core.privileged.exception.ErrorTypeLocator;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

@Feature(ERROR_HANDLING)
@Story(EXCEPTION_MAPPINGS)
public class ErrorTypeLocatorFactoryTestCase {

  private static final List<ComponentIdentifier> ORIGINAL_ERROR_IDENTIFIERS =
      asList(TRANSFORMATION, EXPRESSION, ROUTING, COMPOSITE_ROUTING, COMPOSITE_ROUTING, CONNECTIVITY, VALIDATION,
             DUPLICATE_MESSAGE, RETRY_EXHAUSTED, SECURITY, CLIENT_SECURITY, SERVER_SECURITY, NOT_PERMITTED, OVERLOAD,
             FLOW_BACK_PRESSURE, REDELIVERY_EXHAUSTED, STREAM_MAXIMUM_SIZE_EXCEEDED);
  private ErrorTypeRepository repository = mock(ErrorTypeRepository.class);

  @Before
  public void setup() {
    for (ComponentIdentifier errorIdent : ORIGINAL_ERROR_IDENTIFIERS) {
      when(repository.lookupErrorType(errorIdent)).thenReturn(of(mock(ErrorType.class)));
      when(repository.getErrorType(errorIdent)).thenReturn(of(mock(ErrorType.class)));
    }
    when(repository.getCriticalErrorType()).thenReturn(mock(ErrorType.class));
    when(repository.getErrorType(FATAL)).thenReturn(of(mock(ErrorType.class)));
    when(repository.getErrorType(UNKNOWN)).thenReturn(of(mock(ErrorType.class)));
    when(repository.lookupErrorType(TRANSACTION)).thenReturn(empty());
  }

  @Test
  @Description("If a new Error Mapping is added, and it doesn't contemplate a case of old Extension Model (without the error in the repository) the creating would fail. This is just a backwards-check test")
  @Issue("W-14608096")
  public void canCreateLocatorWithOriginalErrors() {
    createDefaultErrorTypeLocator(repository);
  }

  @Test
  @Description("Check that the newly error mapping for TransactionException is available even if the error type is not in the repository")
  @Issue("W-14608096")
  public void transactionErrorIsAddedIfNotAvailable() {
    ErrorType anyError = builder().namespace(ANY.getNamespace()).identifier(ANY.getName()).build();
    ErrorTypeLocator locator = createDefaultErrorTypeLocator(repository);
    ErrorType txError = locator.lookupErrorType(TransactionException.class);
    assertThat(txError.getNamespace(), is(CORE_NAMESPACE_NAME));
    assertThat(txError.getIdentifier(), is(TRANSACTION_ERROR_IDENTIFIER));
    assertThat(txError.getParentErrorType(), is(anyError));
  }

}
