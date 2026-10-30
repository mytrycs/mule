/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.enricher;

import static java.util.Optional.of;
import static com.zernova.runtime.api.meta.model.stereotype.StereotypeModelBuilder.newStereotype;
import static com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase.POST_STRUCTURE;
import static com.zernova.runtime.extension.internal.util.ExtensionNamespaceUtils.getExtensionsNamespace;
import static com.zernova.sdk.api.stereotype.ZernovaStereotypes.CONFIG;
import static com.zernova.sdk.api.stereotype.ZernovaStereotypes.CONNECTION;
import static com.zernova.sdk.api.stereotype.ZernovaStereotypes.PROCESSOR;
import static com.zernova.sdk.api.stereotype.ZernovaStereotypes.SOURCE;

import com.zernova.runtime.api.meta.model.declaration.fluent.ConfigurationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ConnectedDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ConnectionProviderDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.SourceDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.WithOperationsDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.WithSourcesDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.WithStereotypesDeclaration;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;
import com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.WalkingDeclarationEnricher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Adds a default stereotype on all components which don't define one
 *
 * @since 4.5.0
 */
public class DefaultStereotypeEnricher implements WalkingDeclarationEnricher {

  @Override
  public DeclarationEnricherPhase getExecutionPhase() {
    return POST_STRUCTURE;
  }

  @Override
  public Optional<DeclarationEnricherWalkDelegate> getWalkDelegate(ExtensionLoadingContext extensionLoadingContext) {
    return of(new DeclarationEnricherWalkDelegate() {

      final Map<String, StereotypeModel> parentsCache = new HashMap<>();
      final ExtensionDeclaration extensionDeclaration = extensionLoadingContext.getExtensionDeclarer().getDeclaration();
      final String namespace = getExtensionsNamespace(extensionDeclaration);

      @Override
      public void onConfiguration(ConfigurationDeclaration declaration) {
        assureHasStereotype(declaration, () -> createStereotype(namespace, declaration.getName(), CONFIG));
      }

      @Override
      public void onConnectionProvider(ConnectedDeclaration owner, ConnectionProviderDeclaration declaration) {
        assureHasStereotype(declaration, () -> createStereotype(namespace, declaration.getName(), CONNECTION));
      }

      @Override
      public void onOperation(WithOperationsDeclaration owner, OperationDeclaration declaration) {
        assureHasStereotype(declaration, () -> createComponentStereotype(namespace, declaration.getName(), PROCESSOR));
      }

      @Override
      public void onSource(WithSourcesDeclaration owner, SourceDeclaration declaration) {
        assureHasStereotype(declaration, () -> createComponentStereotype(namespace, declaration.getName(), SOURCE));
      }

      private void assureHasStereotype(WithStereotypesDeclaration declaration, Supplier<StereotypeModel> defaultStereotype) {
        if (declaration.getStereotype() == null) {
          declaration.withStereotype(defaultStereotype.get());
        }
      }

      private StereotypeModel createStereotype(String namespace, String name, StereotypeModel parent) {
        return newStereotype(name, namespace)
            .withParent(parent)
            .build();
      }

      private StereotypeModel createComponentStereotype(String namespace, String name, StereotypeModel parent) {
        if (!parent.getNamespace().equals(namespace)) {
          StereotypeModel originalParent = parent;
          parent = parentsCache.computeIfAbsent(parent.getType(),
                                                key -> newStereotype(originalParent.getType(), namespace)
                                                    .withParent(originalParent)
                                                    .build());
        }
        return createStereotype(namespace, name, parent);
      }
    });
  }
}
