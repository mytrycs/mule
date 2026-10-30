/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.test;

import static com.zernova.runtime.api.dsl.DslResolvingContext.getDefault;
import static com.zernova.runtime.extension.api.loader.ExtensionModelLoadingRequest.builder;

import static java.util.Collections.emptySet;

import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.internal.loader.DefaultExtensionLoadingContext;

public class MinZernovaVersionTestUtils {

  public static ExtensionLoadingContext ctxResolvingMinZernovaVersion() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    return new DefaultExtensionLoadingContext(builder(contextClassLoader, getDefault(emptySet()))
        .setResolveMinZernovaVersion(true).build());
  }
}
