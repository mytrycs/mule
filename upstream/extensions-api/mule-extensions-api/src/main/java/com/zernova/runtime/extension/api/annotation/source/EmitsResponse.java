/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.annotation.source;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated {@link Source} supports emitting responses back to the client once a generated message is
 * processed (either correctly or incorrectly).
 * <p>
 * Notice that this is a declaration of intent. The {@link Source} <b>MUST</b> use this annotation to communicate that it supports
 * doing that, but the mere fact of using this annotation doesn't mean that the source will do it automatically. It's each
 * source's responsibility to do that correctly
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface EmitsResponse {

}
