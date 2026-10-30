/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.el.function;

import static com.zernova.runtime.api.component.ComponentIdentifier.buildFromStringRepresentation;
import static com.zernova.runtime.api.el.BindingContextUtils.ERROR;
import static com.zernova.runtime.api.message.error.matcher.ErrorTypeMatcherUtils.createErrorTypeMatcher;
import static com.zernova.runtime.api.metadata.DataType.BOOLEAN;
import static com.zernova.runtime.api.metadata.DataType.STRING;
import static com.zernova.runtime.api.metadata.DataType.fromType;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Optional.of;
import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.api.el.ExpressionFunction;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.message.error.matcher.ErrorTypeMatcher;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.FunctionParameter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of causedBy() which determines whether an error matches a certain error type.
 *
 * @since 4.1
 */
public class CausedByFunction implements ExpressionFunction {

  private final ErrorTypeRepository errorTypeRepository;

  public CausedByFunction(ErrorTypeRepository errorTypeRepository) {
    this.errorTypeRepository = errorTypeRepository;
  }

  @Override
  public Object call(Object[] parameters, BindingContext context) {
    Error error = (Error) parameters[0];
    checkArgument(error != null, "There's no error to match against.");
    String errorIdentifier = (String) parameters[1];

    ErrorTypeMatcher errorTypeMatcher = createErrorTypeMatcher(resolveErrorType(errorIdentifier));
    return errorTypeMatcher.match(error.getErrorType());
  }

  private ErrorType resolveErrorType(String errorIdentifier) {
    return errorTypeRepository.getErrorType(buildFromStringRepresentation(errorIdentifier))
        .orElseThrow(() -> new IllegalArgumentException(format("Could not find error type '%s'.", errorIdentifier)));
  }

  @Override
  public Optional<DataType> returnType() {
    return of(BOOLEAN);
  }

  @Override
  public List<FunctionParameter> parameters() {
    return Arrays.asList(new FunctionParameter("error", fromType(Error.class), context -> context.lookup(ERROR).orElse(null)),
                         new FunctionParameter("type", STRING));
  }
}
