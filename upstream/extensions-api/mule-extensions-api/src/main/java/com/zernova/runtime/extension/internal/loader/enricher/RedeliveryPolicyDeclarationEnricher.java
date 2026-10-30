/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.enricher;

import static com.zernova.metadata.api.utils.MetadataTypeUtils.getTypeId;
import static com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase.WIRING;
import static com.zernova.runtime.extension.internal.loader.util.InfrastructureParameterBuilder.addRedeliveryPolicy;

import static java.util.Optional.of;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.runtime.api.meta.model.ImportedTypeModel;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.SourceDeclaration;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.IdempotentDeclarationEnricherWalkDelegate;
import com.zernova.runtime.extension.api.loader.WalkingDeclarationEnricher;
import com.zernova.runtime.extension.api.property.NoRedeliveryPolicyModelProperty;

import java.util.Optional;

/**
 * A {@link DeclarationEnricher} which adds a redelivery policy parameter to all sources
 *
 * @since 1.5.0
 */
public final class RedeliveryPolicyDeclarationEnricher implements WalkingDeclarationEnricher {

  @Override
  public DeclarationEnricherPhase getExecutionPhase() {
    return WIRING;
  }

  @Override
  public Optional<DeclarationEnricherWalkDelegate> getWalkDelegate(ExtensionLoadingContext extensionLoadingContext) {
    return of(new IdempotentDeclarationEnricherWalkDelegate() {

      ExtensionDeclaration extension = extensionLoadingContext.getExtensionDeclarer().getDeclaration();
      boolean hasObjectStoreParams = false;

      @Override
      protected void onSource(SourceDeclaration declaration) {
        if (declaration.getModelProperty(NoRedeliveryPolicyModelProperty.class).isPresent()) {
          return;
        }

        addRedeliveryPolicy(declaration);
        hasObjectStoreParams = true;
      }

      @Override
      public void onWalkFinished() {
        if (hasObjectStoreParams && !isObjectStoreAlreadyImported(extension)) {
          extension.getImportedTypes()
              .add(new ImportedTypeModel((ObjectType) extensionLoadingContext.getTypeLoader().load(ObjectStore.class)));
        }
      }
    });
  }

  private boolean isObjectStoreAlreadyImported(ExtensionDeclaration extension) {
    return extension.getImportedTypes().stream().anyMatch(model -> isObjectStore(model.getImportedType()));
  }

  private boolean isObjectStore(MetadataType type) {
    return getTypeId(type)
        .filter(typeId -> ObjectStore.class.getName().equals(typeId))
        .isPresent();
  }
}
