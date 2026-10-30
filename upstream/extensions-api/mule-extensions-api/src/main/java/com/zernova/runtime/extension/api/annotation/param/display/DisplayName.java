/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.annotation.param.display;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Allows to specify a custom label for the element and/or field to be used in the UI. If a value is not specified, the name is
 * inferred from the annotated element's name.
 * <p>
 * Use this annotation to override the default inferred nickname for a {@link Parameter}, an operation, an operation's parameter,
 * {@link Source}, {@link Configuration} and {@link ConnectionProvider}.
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
@Target({PARAMETER, FIELD, METHOD, TYPE})
@Retention(RUNTIME)
@Documented
public @interface DisplayName {

  /**
   * The value is a short name for the annotated element. If this value is not specified it will be inferred from the annotated
   * element name.
   */
  String value() default "";
}
