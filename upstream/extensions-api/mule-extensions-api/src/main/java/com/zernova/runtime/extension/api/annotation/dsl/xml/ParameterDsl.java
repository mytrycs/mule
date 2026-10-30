/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.annotation.dsl.xml;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Allows setting directives regarding syntax and semantics of the generated XML DSL.
 *
 * It can be applied on fields annotated with {@link Parameter} or parameters of methods mapping to operations
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Documented
public @interface ParameterDsl {

  /**
   * Whether the associated element should support inline definition as child element. Defaults to {@code true}
   */
  boolean allowInlineDefinition() default true;

  /**
   * Whether the associated element should support registry references. Defaults to {@code true}
   */
  boolean allowReferences() default true;

}
