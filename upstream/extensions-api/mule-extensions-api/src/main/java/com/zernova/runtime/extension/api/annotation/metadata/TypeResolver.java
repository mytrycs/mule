/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.annotation.metadata;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.metadata.resolving.InputTypeResolver;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Associates the annotated Parameter to an {@link InputTypeResolver} that will be used to resolve the Parameter's
 * {@link MetadataType} dynamically
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
@Documented
public @interface TypeResolver {

  /**
   * @return the associated {@link InputTypeResolver} for the annotated Parameter
   */
  Class<? extends InputTypeResolver> value();

}
