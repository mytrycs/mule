/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.internal.util;

import static java.util.Collections.emptySet;
import static com.zernova.runtime.api.dsl.DslResolvingContext.getDefault;
import static com.zernova.runtime.module.extension.internal.loader.java.AbstractJavaExtensionModelLoader.EXTENSION_TYPE;
import static com.zernova.runtime.module.extension.internal.loader.java.AbstractJavaExtensionModelLoader.VERSION;

import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.internal.loader.DefaultExtensionLoadingContext;
import com.zernova.runtime.module.extension.internal.loader.delegate.DefaultExtensionModelLoaderDelegate;
import com.zernova.runtime.module.extension.internal.loader.parser.java.JavaExtensionModelParserFactory;

public final class ExtensionDeclarationTestUtils {

  private ExtensionDeclarationTestUtils() {}

  public static ExtensionDeclarer declarerFor(Class<?> extensionClass, String version) {
    return declarerFor(extensionClass, extensionClass.getClassLoader(), version);
  }

  public static ExtensionDeclarer declarerFor(Class<?> extensionClass, ClassLoader classLoader, String version) {
    DefaultExtensionLoadingContext ctx = new DefaultExtensionLoadingContext(
                                                                            classLoader,
                                                                            getDefault(emptySet()));

    return declarerFor(extensionClass, version, ctx);
  }

  public static ExtensionDeclarer declarerFor(Class<?> extensionClass, String version, ExtensionLoadingContext context) {
    context.addParameter(VERSION, version);
    context.addParameter(EXTENSION_TYPE, extensionClass);

    return javaDeclarerFor(version, context);
  }

  public static ExtensionDeclarer javaDeclarerFor(String version, ExtensionLoadingContext context) {
    return new DefaultExtensionModelLoaderDelegate(version).declare(new JavaExtensionModelParserFactory(), context);
  }
}
