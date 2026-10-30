/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.enricher;

import static com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase.STRUCTURE;
import static com.zernova.runtime.extension.api.util.ExtensionModelUtils.acceptsExpressions;
import static com.zernova.runtime.extension.internal.loader.util.InfrastructureParameterBuilder.addExpirationPolicy;

import com.zernova.runtime.api.meta.model.declaration.fluent.ConfigurationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterizedDeclaration;
import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.runtime.ExpirationPolicy;

/**
 * Adds an {@link ExpirationPolicy} parameter to all configs which might be used in a dynamic way
 *
 * @since 1.0
 */
public class DynamicConfigDeclarationEnricher implements DeclarationEnricher {

  @Override
  public DeclarationEnricherPhase getExecutionPhase() {
    return STRUCTURE;
  }

  @Override
  public void enrich(ExtensionLoadingContext extensionLoadingContext) {
    extensionLoadingContext.getExtensionDeclarer().getDeclaration().getConfigurations().forEach(config -> {
      if (canBeDynamic(config)) {
        addExpirationPolicy(config);
      }
    });
  }

  private boolean canBeDynamic(ConfigurationDeclaration config) {
    if (hasAnyDynamic(config)) {
      return true;
    }

    return config.getConnectionProviders().stream().anyMatch(this::hasAnyDynamic);
  }

  private boolean hasAnyDynamic(ParameterizedDeclaration<?> declaration) {
    return declaration.getAllParameters().stream().anyMatch(p -> acceptsExpressions(p.getExpressionSupport()));
  }
}
