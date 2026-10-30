/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.annotation.notification;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.notification.NotificationActionDefinition;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for {@link Extension} level to communicate and declare the {@link NotificationActionDefinition}s that the whole
 * extension emits.
 *
 * @since 1.1
 */
@MinZernovaVersion("4.1")
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface NotificationActions {

  /**
   * @return an {@link Enum} class featuring all {@link NotificationActionDefinition}
   */
  Class<? extends Enum<? extends NotificationActionDefinition<? extends Enum>>> value();

}
