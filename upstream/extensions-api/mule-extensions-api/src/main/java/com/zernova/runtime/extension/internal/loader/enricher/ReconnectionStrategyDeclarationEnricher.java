/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.enricher;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase.STRUCTURE;
import static com.zernova.runtime.extension.internal.loader.util.InfrastructureParameterBuilder.addReconnectionStrategyParameter;
import static com.zernova.runtime.extension.privileged.util.ComponentDeclarationUtils.isReconnectionStrategySupported;

import com.zernova.runtime.api.meta.model.declaration.fluent.ExecutableComponentDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.SourceDeclaration;
import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.IdempotentDeclarationEnricherWalkDelegate;
import com.zernova.runtime.extension.api.loader.WalkingDeclarationEnricher;

import java.util.Optional;

/**
 * A {@link DeclarationEnricher} which adds the following to all {@link SourceDeclaration}:
 *
 * <ul>
 * <li>A reconnection strategy parameter</li>
 * </ul>
 *
 * @since 1.0
 */
public final class ReconnectionStrategyDeclarationEnricher implements WalkingDeclarationEnricher {

  @Override
  public DeclarationEnricherPhase getExecutionPhase() {
    return STRUCTURE;
  }

  @Override
  public Optional<DeclarationEnricherWalkDelegate> getWalkDelegate(ExtensionLoadingContext extensionLoadingContext) {
    final ExtensionDeclaration declaration = extensionLoadingContext.getExtensionDeclarer().getDeclaration();

    if (isReconnectionStrategySupported(declaration)) {
      return of(new IdempotentDeclarationEnricherWalkDelegate() {

        @Override
        protected void onSource(SourceDeclaration declaration) {
          addReconnectionIfRequired(declaration);
        }

        @Override
        protected void onOperation(OperationDeclaration declaration) {
          addReconnectionIfRequired(declaration);
        }

        private void addReconnectionIfRequired(ExecutableComponentDeclaration<?> declaration) {
          if (declaration.isRequiresConnection() && isReconnectionStrategySupported(declaration)) {
            addReconnectionStrategyParameter(declaration);
          }
        }
      });
    }

    return empty();
  }
}
