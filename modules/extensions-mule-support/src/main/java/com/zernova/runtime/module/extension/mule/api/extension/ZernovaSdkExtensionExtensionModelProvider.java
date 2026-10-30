/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.api.extension;

import static com.zernova.runtime.api.dsl.DslResolvingContext.nullDslResolvingContext;
import static com.zernova.runtime.extension.api.loader.ExtensionModelLoadingRequest.builder;

import com.zernova.api.annotation.Experimental;
import com.zernova.api.annotation.NoInstantiate;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;

/**
 * Utility class to access the {@link ExtensionModel} definition for Mule SDK Extensions
 *
 * @since 4.5
 */
@Experimental
@NoInstantiate
public class ZernovaSdkExtensionExtensionModelProvider extends ExtensionModelLoader {

  private static final LazyValue<ExtensionModel> EXTENSION_MODEL =
      new LazyValue<>(() -> new ZernovaSdkExtensionExtensionModelProvider()
          .loadExtensionModel(new ZernovaSdkExtensionExtensionModelDeclarer().declareExtensionModel(),
                              builder(ZernovaSdkExtensionExtensionModelProvider.class.getClassLoader(),
                                      nullDslResolvingContext())
                                          .build()));

  @Override
  protected void declareExtension(ExtensionLoadingContext context) {
    // nothing to do
  }

  @Override
  public String getId() {
    return "mule-muleSdk";
  }

  /**
   * @return the {@link ExtensionModel} definition.
   */
  public static ExtensionModel getExtensionModel() {
    return EXTENSION_MODEL.get();
  }
}
