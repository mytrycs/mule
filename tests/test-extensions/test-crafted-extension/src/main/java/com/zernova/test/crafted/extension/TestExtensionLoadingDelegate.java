/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.crafted.extension;

import static com.zernova.runtime.api.meta.Category.COMMUNITY;
import static com.zernova.runtime.extension.api.ExtensionConstants.ALL_SUPPORTED_JAVA_VERSIONS;

import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingDelegate;

public class TestExtensionLoadingDelegate implements ExtensionLoadingDelegate {

  private static final String EXTENSION_NAME = "crafted-extension";

  @Override
  public void accept(ExtensionDeclarer extensionDeclarer, ExtensionLoadingContext context) {
    extensionDeclarer.named(EXTENSION_NAME)
        .describedAs("Crafted Extension")
        .onVersion("1.0.0")
        .supportingJavaVersions(ALL_SUPPORTED_JAVA_VERSIONS)
        .withCategory(COMMUNITY)
        .fromVendor("Mulesoft");
  }
}
