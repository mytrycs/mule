/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.loader.delegate;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.parser.ExtensionModelParserFactory;

/**
 * Contract for classes that creates an {@link ExtensionDeclarer} from a {@link ExtensionLoadingContext}.
 *
 * @since 1.10.0
 */
@NoImplement
public interface ModelLoaderDelegate {

  /**
   * Creates and populates an {@link ExtensionDeclarer} using the given {@code context} and {@code parserFactory}
   *
   * @param parserFactory the {@link ExtensionModelParserFactory} used to read the extension's declaration
   * @param context       an {@link ExtensionLoadingContext} instance.
   * @return a built {@link ExtensionDeclarer}.
   */
  ExtensionDeclarer declare(ExtensionModelParserFactory parserFactory, ExtensionLoadingContext context);
}
