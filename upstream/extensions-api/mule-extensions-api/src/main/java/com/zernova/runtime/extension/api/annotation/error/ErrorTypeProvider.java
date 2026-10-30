/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.annotation.error;

import com.zernova.runtime.extension.api.error.ErrorTypeDefinition;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.util.Set;

/**
 * A component responsible for providing an idempotent {@link Set} of {@link ErrorTypeDefinition} representing the list of errors
 * that a component can throw.
 *
 * @since 1.0
 * @see Throws
 * @see ErrorTypeDefinition
 */
@MinZernovaVersion("4.1")
public interface ErrorTypeProvider {

  /**
   * @return a {@link Set} of {@link ErrorTypeDefinition}
   */
  Set<ErrorTypeDefinition> getErrorTypes();
}
