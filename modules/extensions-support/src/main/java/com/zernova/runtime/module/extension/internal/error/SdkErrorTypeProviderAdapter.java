/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.error;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.sdk.api.annotation.error.ErrorTypeProvider;
import com.zernova.sdk.api.error.ErrorTypeDefinition;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Adapts a legacy {@link com.zernova.runtime.extension.api.annotation.error.ErrorTypeProvider} into an sdk-api
 * {@link ErrorTypeProvider}
 *
 * @since 4.5.0
 */
public class SdkErrorTypeProviderAdapter implements ErrorTypeProvider {

  /**
   * Returns an adapted version of the {@code value}.
   *
   * If the {@code value} doesn't need adapting because it's already an {@link ErrorTypeProvider}, then the same instance is
   * returned
   *
   * @param value the value to adapt
   * @return an adapted value or the same instance if no adaptation needed
   * @throws IllegalArgumentException if the value is not an instance of an adaptable type
   */
  public static ErrorTypeProvider from(Object value) {
    checkArgument(value != null, "Cannot adapt null value");

    if (value instanceof ErrorTypeProvider) {
      return (ErrorTypeProvider) value;
    } else if (value instanceof com.zernova.runtime.extension.api.annotation.error.ErrorTypeProvider) {
      return new SdkErrorTypeProviderAdapter((com.zernova.runtime.extension.api.annotation.error.ErrorTypeProvider) value);
    } else {
      throw new IllegalArgumentException(String.format("Value of class '%s' is neither a '%s' nor a '%s'",
                                                       value.getClass().getName(),
                                                       ErrorTypeProvider.class.getName(),
                                                       com.zernova.runtime.extension.api.annotation.error.ErrorTypeProvider.class
                                                           .getName()));
    }
  }

  private final com.zernova.runtime.extension.api.annotation.error.ErrorTypeProvider delegate;

  private SdkErrorTypeProviderAdapter(com.zernova.runtime.extension.api.annotation.error.ErrorTypeProvider delegate) {
    this.delegate = delegate;
  }

  @Override
  public Set<ErrorTypeDefinition> getErrorTypes() {
    Set<ErrorTypeDefinition> errors = new LinkedHashSet<>();
    for (com.zernova.runtime.extension.api.error.ErrorTypeDefinition def : delegate.getErrorTypes()) {
      errors.add(new SdkErrorTypeDefinitionAdapter(def));
    }

    return errors;
  }
}
