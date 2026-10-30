/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.loader;

import static com.zernova.runtime.extension.api.ExtensionConstants.MULE_SDK_APPLICATION_LOADER_ID;

import com.zernova.runtime.extension.api.loader.AbstractParserBasedExtensionModelLoader;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;
import com.zernova.runtime.extension.api.loader.parser.ExtensionModelParserFactory;
import com.zernova.runtime.module.extension.mule.internal.loader.parser.ZernovaSdkApplicationExtensionModelParserFactory;

/**
 * {@link ExtensionModelLoader} implementation for Mule SDK Extensions defined as part of the same artifact.
 *
 * @since 4.5.0
 */
public class ZernovaSdkApplicationExtensionModelLoader extends AbstractParserBasedExtensionModelLoader {

  @Override
  public String getId() {
    return MULE_SDK_APPLICATION_LOADER_ID;
  }

  @Override
  protected ExtensionModelParserFactory getExtensionModelParserFactory(ExtensionLoadingContext context) {
    return new ZernovaSdkApplicationExtensionModelParserFactory();
  }
}
