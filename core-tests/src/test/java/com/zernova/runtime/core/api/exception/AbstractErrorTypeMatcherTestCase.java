/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.exception;

import static com.zernova.runtime.core.api.error.Errors.CORE_NAMESPACE_NAME;
import static com.zernova.runtime.core.api.error.Errors.Identifiers.EXPRESSION_ERROR_IDENTIFIER;
import static com.zernova.runtime.core.api.error.Errors.Identifiers.TRANSFORMATION_ERROR_IDENTIFIER;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import org.junit.Before;

public abstract class AbstractErrorTypeMatcherTestCase extends AbstractZernovaContextTestCase {

  protected ErrorType anyErrorType;
  protected ErrorType transformationErrorType;
  protected ErrorType expressionErrorType;

  @Before
  public void setUp() {
    ErrorTypeRepository errorTypeRepository = muleContext.getErrorTypeRepository();
    anyErrorType = errorTypeRepository.getAnyErrorType();
    ComponentIdentifier transformationIdentifier =
        ComponentIdentifier.builder().name(TRANSFORMATION_ERROR_IDENTIFIER).namespace(CORE_NAMESPACE_NAME)
            .build();
    transformationErrorType = errorTypeRepository.lookupErrorType(transformationIdentifier).get();
    ComponentIdentifier expressionIdentifier =
        ComponentIdentifier.builder().name(EXPRESSION_ERROR_IDENTIFIER).namespace(CORE_NAMESPACE_NAME).build();
    expressionErrorType = errorTypeRepository.lookupErrorType(expressionIdentifier).get();
  }

}
