/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.enricher;

import static com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase.INITIALIZE;

import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.property.ClassLoaderModelProperty;

/**
 * Adds a {@link ClassLoaderModelProperty} pointing to {@link ExtensionLoadingContext#getExtensionClassLoader()}
 *
 * @since 1.0
 */
public class ClassLoaderDeclarationEnricher implements DeclarationEnricher {

  @Override
  public DeclarationEnricherPhase getExecutionPhase() {
    return INITIALIZE;
  }

  @Override
  public void enrich(ExtensionLoadingContext extensionLoadingContext) {
    ClassLoader classLoader = extensionLoadingContext.getExtensionClassLoader();
    if (classLoader == null) {
      throw new IllegalModelDefinitionException("No ClassLoader was specified for extension " +
          extensionLoadingContext.getExtensionDeclarer().getDeclaration().getName());
    }

    extensionLoadingContext.getExtensionDeclarer().withModelProperty(new ClassLoaderModelProperty(classLoader));
  }
}
