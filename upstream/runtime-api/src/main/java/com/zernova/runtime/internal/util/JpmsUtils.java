/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.internal.util;

/**
 * No-op implementation of {@code JpmsUtils} to use when running on JVM 8.
 *
 * @since 1.5
 */
public class JpmsUtils {

  public static boolean isMemberOfNamedModule(Class<?> caller) {
    // Nothing to do
    return false;
  }

}
