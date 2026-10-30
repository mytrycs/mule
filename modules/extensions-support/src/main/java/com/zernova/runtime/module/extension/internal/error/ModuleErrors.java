/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.error;

import com.zernova.sdk.api.error.ErrorTypeDefinition;
import com.zernova.sdk.api.error.ZernovaErrors;

import java.util.Optional;

public enum ModuleErrors implements ErrorTypeDefinition<ModuleErrors> {

  CONNECTIVITY {

    @Override
    public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
      return Optional.of(ZernovaErrors.CONNECTIVITY);
    }
  },
  RETRY_EXHAUSTED {

    @Override
    public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
      return Optional.of(ZernovaErrors.RETRY_EXHAUSTED);
    }
  }
}
