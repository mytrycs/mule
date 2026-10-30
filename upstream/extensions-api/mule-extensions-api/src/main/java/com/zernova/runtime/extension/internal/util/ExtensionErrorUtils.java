/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.util;

import static com.zernova.runtime.extension.api.error.ZernovaErrors.VALIDATION;

import com.zernova.runtime.api.meta.model.error.ErrorModel;
import com.zernova.sdk.api.error.ZernovaErrors;

import java.util.Collection;
import java.util.Optional;

/**
 * Utilities for handling extension errors.
 * <p>
 * This class <b>IS NOT</b> part of the API. To be used by the Mule Runtime only
 *
 * @since 1.5.0
 */
public final class ExtensionErrorUtils {

  /**
   * Traverses the {@code errors} collection and returns the first item which represents a {@link ZernovaErrors#VALIDATION} error
   *
   * @param errors a set of {@link ErrorModel}
   * @return an optional {@link ErrorModel}
   */
  public static Optional<ErrorModel> getValidationError(Collection<ErrorModel> errors) {
    return errors.stream()
        .filter(ExtensionErrorUtils::isValidationError)
        .findAny();
  }

  private static boolean isValidationError(ErrorModel errorModel) {
    if (VALIDATION.getType().equals(errorModel.getType()) &&
        "ZERNOVA".equals(errorModel.getNamespace())) {
      return true;
    }

    return errorModel.getParent().map(ExtensionErrorUtils::isValidationError).orElse(false);
  }

  private ExtensionErrorUtils() {}
}
