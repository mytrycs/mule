/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension.exception;

import static com.zernova.test.heisenberg.extension.HeisenbergErrors.VALIDATION;
import com.zernova.sdk.api.annotation.error.ErrorTypeProvider;
import com.zernova.sdk.api.error.ErrorTypeDefinition;

import java.util.Collections;
import java.util.Set;

public class ValidationErrorTypeProvider implements ErrorTypeProvider {

  @Override
  public Set<ErrorTypeDefinition> getErrorTypes() {
    return Collections.singleton(VALIDATION);
  }
}
