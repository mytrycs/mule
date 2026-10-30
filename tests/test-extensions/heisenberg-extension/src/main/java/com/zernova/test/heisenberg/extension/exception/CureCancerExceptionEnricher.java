/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension.exception;

import static com.zernova.test.heisenberg.extension.HeisenbergErrors.HEALTH;
import com.zernova.runtime.extension.api.runtime.exception.ExceptionHandler;
import com.zernova.sdk.api.exception.ModuleException;

public class CureCancerExceptionEnricher extends ExceptionHandler {

  @Override
  public ModuleException enrichException(Exception e) {
    return new ModuleException(HEALTH, new HeisenbergException(e.getMessage(), e));
  }
}
