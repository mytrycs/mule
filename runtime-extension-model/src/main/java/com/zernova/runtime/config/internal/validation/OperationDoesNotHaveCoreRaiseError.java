/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.api.component.ComponentIdentifier.builder;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.CORE_PREFIX;

import com.zernova.runtime.api.component.ComponentIdentifier;

public class OperationDoesNotHaveCoreRaiseError extends ZernovaSdkOperationDoesNotHaveForbiddenComponents {

  private static final String CORE_RAISE_ERROR_ELEMENT = "raise-error";
  private static final ComponentIdentifier CORE_RAISE_ERROR_IDENTIFIER =
      builder().namespace(CORE_PREFIX).name(CORE_RAISE_ERROR_ELEMENT).build();

  @Override
  protected ComponentIdentifier forbiddenComponentIdentifier() {
    return CORE_RAISE_ERROR_IDENTIFIER;
  }
}
