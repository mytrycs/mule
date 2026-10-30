/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.api.component;

import static java.lang.String.format;
import static com.zernova.runtime.api.component.ComponentIdentifier.buildFromStringRepresentation;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.DefaultZernovaException;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.api.exception.TypedException;
import com.zernova.runtime.core.api.processor.Processor;

import jakarta.inject.Inject;

/**
 * Processor that throws the specified exception. Can be refactored to throw errors later on
 *
 * @since 4.0
 */
public class ThrowProcessor extends AbstractComponent implements Processor {

  private static final String EXCEPTION_ERROR = "If an error is not provided, the provided exception must be a TypedException.";

  private Class<? extends Throwable> exception;
  private String error;
  private volatile int count = -1;

  @Inject
  private ZernovaContext muleContext;

  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    if (count == -1 || count-- > 0) {
      try {
        Throwable instantiatedException = exception.newInstance();
        if (error != null) {
          ErrorTypeRepository errorTypeRepository = muleContext.getErrorTypeRepository();
          ErrorType errorType = errorTypeRepository.lookupErrorType(buildFromStringRepresentation(error))
              .orElseThrow(() -> new DefaultZernovaException(format("Could not find error: '%s'", error)));
          throw new TypedException(instantiatedException, errorType);
        } else {
          checkArgument(instantiatedException instanceof TypedException, EXCEPTION_ERROR);
          throw (TypedException) instantiatedException;
        }
      } catch (InstantiationException | IllegalAccessException e) {
        throw new DefaultZernovaException(format("Failed to instantiate exception class '%s'", exception.getSimpleName()));
      }
    } else {
      return event;
    }
  }

  public void setException(Class<? extends Throwable> exception) {
    this.exception = exception;
  }

  public void setError(String error) {
    this.error = error;
  }

  public void setCount(int count) {
    this.count = count;
  }
}
