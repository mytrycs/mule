/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.annotation.error;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.error.ErrorTypeDefinition;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Declares an array of {@link Class classes} of an {@link ErrorTypeProvider}, to communicate and declare which
 * {@link ErrorTypeDefinition} the operation could throw.
 * <p>
 * This annotation can be applied to an Operation method, an Operation container class or an {@link Extension} class. The
 * annotation value to consider is the one of in the deepest level, eg: If the annotation is used in an operation method and in an
 * extension class, the one to use is the one in the operation.
 *
 * @since 1.0
 * @see ErrorTypeProvider
 * @see ErrorTypeDefinition
 */
@MinZernovaVersion("4.1")
@Target({METHOD, TYPE})
@Retention(RUNTIME)
@Documented
public @interface Throws {

  Class<? extends ErrorTypeProvider>[] value();
}
