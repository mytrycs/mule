/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.error;

import static java.util.Optional.empty;

import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.util.Optional;

/**
 * Identifies a Mule Error Type to be declared by extensions and must be implemented by an {@link Enum}.
 * <p>
 * <b>Error Type Hierarchy</b>
 * <ul>
 * <li>To declare hierarchy between error types, the values of the {@link Enum} can override
 * {@link ErrorTypeDefinition#getParent()} and declare which is the parent of the modified element.</li>
 * <li>The parent can be any of the same {@link Enum} class or any of {@link ZernovaErrors} values, inherit from {@link ZernovaErrors}
 * indicates that the child will inherit from a Mule exposed error.</li>
 * <li>Cyclic references are not permitted.</li>
 * </ul>
 *
 * @param <E> Enum type
 * @see ZernovaErrors
 * @since 1.0
 */
@MinZernovaVersion("4.1")
public interface ErrorTypeDefinition<E extends Enum<E>> {

  /**
   * @return The type of the current error type definition
   */
  default String getType() {
    return ((E) this).name();
  }

  /**
   * @return The {@link Optional} parent of the current error type definition
   */
  default Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
    return empty();
  }
}
