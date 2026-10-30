/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension;

import static java.util.Optional.ofNullable;

import com.zernova.sdk.api.error.ErrorTypeDefinition;
import com.zernova.sdk.api.error.ZernovaErrors;

import java.util.Optional;

public enum HeisenbergErrors implements ErrorTypeDefinition<HeisenbergErrors> {

  CONNECTIVITY(ZernovaErrors.CONNECTIVITY), OAUTH2(HeisenbergErrors.CONNECTIVITY), HEALTH(HeisenbergErrors.CONNECTIVITY), VALIDATION(
      ZernovaErrors.VALIDATION);

  private ErrorTypeDefinition<?> parentErrortype;

  HeisenbergErrors(ErrorTypeDefinition parentErrorType) {
    this.parentErrortype = parentErrorType;
  }

  HeisenbergErrors() {}

  @Override
  public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
    return ofNullable(parentErrortype);
  }
}
