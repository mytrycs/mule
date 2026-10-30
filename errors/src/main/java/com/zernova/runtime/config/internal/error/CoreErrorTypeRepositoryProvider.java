/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.error;

import static com.zernova.runtime.config.internal.error.ZernovaCoreErrorTypeRepository.MULE_CORE_ERROR_TYPE_REPOSITORY;

import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.ast.api.error.ErrorTypeRepositoryProvider;

public class CoreErrorTypeRepositoryProvider implements ErrorTypeRepositoryProvider {

  @Override
  public ErrorTypeRepository get() {
    return MULE_CORE_ERROR_TYPE_REPOSITORY;
  }
}
