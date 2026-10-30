/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.enricher;

import static java.util.Optional.empty;
import static com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase.INITIALIZE;
import static com.zernova.runtime.extension.api.util.XmlModelUtils.createXmlLanguageModel;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;

public final class XmlDeclarationEnricher implements DeclarationEnricher {

  @Override
  public DeclarationEnricherPhase getExecutionPhase() {
    return INITIALIZE;
  }

  @Override
  public void enrich(ExtensionLoadingContext extensionLoadingContext) {
    final ExtensionDeclarer extensionDeclarer = extensionLoadingContext.getExtensionDeclarer();
    ExtensionDeclaration extensionDeclaration = extensionDeclarer.getDeclaration();
    if (extensionDeclaration.getXmlDslModel() == null) {
      extensionDeclarer.withXmlDsl(createXmlLanguageModel(empty(), empty(), extensionDeclaration.getName(),
                                                          extensionDeclaration.getVersion()));
    }
  }
}
