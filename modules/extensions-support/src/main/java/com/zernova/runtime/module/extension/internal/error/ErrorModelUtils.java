/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.error;

import com.zernova.runtime.api.meta.model.error.ErrorModel;
import com.zernova.sdk.api.error.ErrorTypeDefinition;
import com.zernova.sdk.api.error.ZernovaErrors;

/**
 * {@link ErrorModel} handling utilities
 */
public final class ErrorModelUtils {

  /**
   * @param errorType an {@link ErrorTypeDefinition}
   * @return whether the given {@code errorType} represents a core mule language error ({@code true}) or a custom one
   *         ({@code false})
   */
  public static boolean isMuleError(ErrorTypeDefinition errorType) {
    if (errorType instanceof SdkErrorTypeDefinitionAdapter) {
      return ((SdkErrorTypeDefinitionAdapter<?>) errorType)
          .getDelegate() instanceof com.zernova.runtime.extension.api.error.ZernovaErrors;
    }

    return errorType instanceof ZernovaErrors;
  }

  private ErrorModelUtils() {}
}
