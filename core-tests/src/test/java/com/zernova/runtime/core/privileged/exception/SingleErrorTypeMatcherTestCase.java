/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.exception;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.core.api.error.Errors.CORE_NAMESPACE_NAME;
import static com.zernova.test.allure.AllureConstants.ErrorHandlingFeature.ERROR_HANDLING;
import static com.zernova.test.allure.AllureConstants.ErrorHandlingFeature.ErrorHandlingStory.ERROR_TYPES;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.ast.internal.error.DefaultErrorTypeBuilder;
import com.zernova.runtime.core.api.exception.AbstractErrorTypeMatcherTestCase;
import com.zernova.runtime.core.api.exception.ErrorTypeMatcher;
import com.zernova.runtime.core.api.exception.SingleErrorTypeMatcher;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(ERROR_HANDLING)
@Story(ERROR_TYPES)
public class SingleErrorTypeMatcherTestCase extends AbstractErrorTypeMatcherTestCase {

  @Test
  public void anyMatchsAll() {
    ErrorType mockErrorType = mock(ErrorType.class);
    when(mockErrorType.getParentErrorType()).thenReturn(transformationErrorType);
    ErrorTypeMatcher anyMatcher = new SingleErrorTypeMatcher(anyErrorType);

    assertThat(anyMatcher.match(anyErrorType), is(true));
    assertThat(anyMatcher.match(transformationErrorType), is(true));
    assertThat(anyMatcher.match(expressionErrorType), is(true));
    assertThat(anyMatcher.match(mockErrorType), is(true));
  }

  @Test
  public void matchEqual() {
    ErrorTypeMatcher transformationMatcher = new SingleErrorTypeMatcher(transformationErrorType);

    assertThat(transformationMatcher.match(transformationErrorType), is(true));
  }

  @Test
  public void matchChild() {
    ComponentIdentifier customTransformerIdentifier =
        ComponentIdentifier.builder().name("custom").namespace(CORE_NAMESPACE_NAME).build();
    ErrorType customTransformerErrorType = DefaultErrorTypeBuilder.builder()
        .namespace(customTransformerIdentifier.getNamespace())
        .identifier(customTransformerIdentifier.getName())
        .parentErrorType(transformationErrorType)
        .build();

    ErrorTypeMatcher transformationMatcher = new SingleErrorTypeMatcher(transformationErrorType);

    assertThat(transformationMatcher.match(customTransformerErrorType), is(true));
  }

  @Test
  public void doesNotMatchParent() {
    ErrorTypeMatcher transformationMatcher = new SingleErrorTypeMatcher(transformationErrorType);

    assertThat(transformationMatcher.match(anyErrorType), is(false));
  }

  @Test
  public void doesNotMatchSibling() {
    ErrorTypeMatcher transformationMatcher = new SingleErrorTypeMatcher(transformationErrorType);

    assertThat(transformationMatcher.match(expressionErrorType), is(false));
  }

}
