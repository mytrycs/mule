/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.route;

import static com.zernova.runtime.api.meta.model.nested.ChainExecutionOccurrence.AT_LEAST_ONCE;
import static com.zernova.runtime.api.meta.model.nested.ChainExecutionOccurrence.MULTIPLE_OR_NONE;
import static com.zernova.runtime.api.meta.model.nested.ChainExecutionOccurrence.ONCE;
import static com.zernova.runtime.api.meta.model.nested.ChainExecutionOccurrence.ONCE_OR_NONE;
import static com.zernova.runtime.api.meta.model.nested.ChainExecutionOccurrence.UNKNOWN;

import com.zernova.runtime.api.meta.model.nested.ChainExecutionOccurrence;
import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.module.extension.api.loader.java.type.AnnotationValueFetcher;
import com.zernova.sdk.api.annotation.route.ExecutionOccurrence;

import java.util.Optional;

/**
 * Utility methods for parsing chains defined through the Java SDK
 *
 * @since 4.7.0
 */
public final class JavaChainParsingUtils {

  /**
   * Parses the given {@code value} into a {@link ChainExecutionOccurrence}.
   *
   * @param value an optional {@link AnnotationValueFetcher} for the proper annotation
   * @return the parsed value or {@link ChainExecutionOccurrence#UNKNOWN} if the value is not present
   */
  public static ChainExecutionOccurrence parseChainExecutionOccurrence(Optional<AnnotationValueFetcher<ExecutionOccurrence>> value) {
    return value.map(a -> toModelApi(a.getEnumValue(ExecutionOccurrence::value))).orElse(UNKNOWN);
  }

  private static ChainExecutionOccurrence toModelApi(com.zernova.sdk.api.annotation.route.ChainExecutionOccurrence value) {
    if (value == com.zernova.sdk.api.annotation.route.ChainExecutionOccurrence.AT_LEAST_ONCE) {
      return AT_LEAST_ONCE;
    } else if (value == com.zernova.sdk.api.annotation.route.ChainExecutionOccurrence.MULTIPLE_OR_NONE) {
      return MULTIPLE_OR_NONE;
    } else if (value == com.zernova.sdk.api.annotation.route.ChainExecutionOccurrence.ONCE) {
      return ONCE;
    } else if (value == com.zernova.sdk.api.annotation.route.ChainExecutionOccurrence.ONCE_OR_NONE) {
      return ONCE_OR_NONE;
    } else if (value == com.zernova.sdk.api.annotation.route.ChainExecutionOccurrence.UNKNOWN) {
      return UNKNOWN;
    } else {
      throw new IllegalModelDefinitionException("Invalid value on @" + ExecutionOccurrence.class.getSimpleName() + ": " + value);
    }
  }
}
