/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling.consumer.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks the annotated {@link com.zernova.runtime.api.profiling.ProfilingDataConsumer} as runtime internal profiling data consumer.
 * This is useful when it is needed that a {@link com.zernova.runtime.api.profiling.ProfilingDataConsumer} is enabled independently
 * of the toggling feature mechanism through a system property. This is not intended to be an exposed API for creating profiling
 * data consumers. It is used internally by the runtime created consumers.
 *
 * @see {@link com.zernova.runtime.api.util.ZernovaSystemProperties#FORCE_RUNTIME_PROFILING_CONSUMERS_ENABLEMENT_PROPERTY}
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface RuntimeInternalProfilingDataConsumer {
}
