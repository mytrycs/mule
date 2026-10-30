/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.annotation.metadata;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.resolving.InputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.TypeKeysResolver;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a field inside a POJO that represents a composed {@link MetadataKey} as one of the parts of that composed
 * {@link MetadataKey}.
 * <p>
 * Multiple {@link MetadataKeyPart}s describe a POJO that can be annotated with {@link MetadataKeyId} and also injected into a
 * {@link InputTypeResolver} or {@link OutputTypeResolver}.
 * <p>
 * {@link MetadataKeyPart} annotated fields must be of type {@link String}
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
@Target({FIELD})
@Retention(RUNTIME)
@Documented
public @interface MetadataKeyPart {

  /**
   * The resolution order of this key part, starting from {@code 1}.
   *
   * @return the resolution order of this key part during the building of the {@link MetadataKeyId} annotated parameter
   *         corresponding to this {@link MetadataKeyPart}.
   */
  int order();

  /**
   * @return whether or not {@code this} {@link MetadataKeyPart} will be provided by the {@link TypeKeysResolver} associated to
   *         the container of this part, or if {@code this} part has no predefined values and has to be provided by the user
   *         entirely.
   */
  boolean providedByKeyResolver() default true;

}
