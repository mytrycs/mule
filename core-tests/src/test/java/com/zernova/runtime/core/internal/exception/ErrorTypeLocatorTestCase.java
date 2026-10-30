/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.exception;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static com.zernova.runtime.ast.api.error.ErrorTypeRepositoryProvider.getCoreErrorTypeRepo;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.UNKNOWN;
import static com.zernova.test.allure.AllureConstants.ErrorHandlingFeature.ERROR_HANDLING;
import static com.zernova.test.allure.AllureConstants.ErrorHandlingFeature.ErrorHandlingStory.EXCEPTION_MAPPINGS;

import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.core.api.exception.ExceptionMapper;
import com.zernova.runtime.core.privileged.exception.ErrorTypeLocator;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(ERROR_HANDLING)
@Story(EXCEPTION_MAPPINGS)
public class ErrorTypeLocatorTestCase extends AbstractZernovaTestCase {

  private final ErrorTypeRepository repository = getCoreErrorTypeRepo();

  @Test
  public void useDefaultErrorWhenNoMappingFound() {
    ErrorType mockedError = mock(ErrorType.class);
    ErrorType unknown = repository.getErrorType(UNKNOWN).get();
    ErrorTypeLocator locator = ErrorTypeLocator.builder(repository)
        .defaultExceptionMapper(ExceptionMapper.builder().addExceptionMapping(Exception.class, mockedError).build())
        .defaultError(unknown)
        .build();

    ErrorType expectedError = locator.lookupErrorType(Exception.class);
    assertThat(expectedError, is(sameInstance(mockedError)));

    ErrorType defaultError = locator.lookupErrorType(Throwable.class);
    assertThat(defaultError, is(sameInstance(unknown)));
  }
}
