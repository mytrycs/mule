/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import com.zernova.runtime.extension.api.error.ErrorTypeDefinition;
import com.zernova.runtime.extension.api.error.ZernovaErrors;

import java.util.Optional;

public enum PetstoreErrorTypeDefinition implements ErrorTypeDefinition<PetstoreErrorTypeDefinition> {

  CONNECTIVITY(ZernovaErrors.CONNECTIVITY), PET_STORE_CONNECTIVITY(CONNECTIVITY), PET_ERROR;

  private ErrorTypeDefinition<? extends Enum<?>> parentError;

  private PetstoreErrorTypeDefinition(ErrorTypeDefinition<? extends Enum<?>> parentError) {
    this.parentError = parentError;
  }

  private PetstoreErrorTypeDefinition() {}

  public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
    return Optional.ofNullable(this.parentError);
  }
}
