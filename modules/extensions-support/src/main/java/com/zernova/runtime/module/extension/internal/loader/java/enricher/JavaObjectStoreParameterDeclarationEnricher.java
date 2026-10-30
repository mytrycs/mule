/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.enricher;

import static java.util.Optional.of;
import static com.zernova.metadata.api.utils.MetadataTypeUtils.getTypeId;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.OBJECT_STORE;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.runtime.api.meta.model.ImportedTypeModel;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterGroupDeclaration;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.IdempotentDeclarationEnricherWalkDelegate;
import com.zernova.runtime.extension.api.loader.WalkingDeclarationEnricher;
import com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes;

import java.util.List;
import java.util.Optional;

/**
 * Enriches all parameters of type {@link ObjectStore} adding the {@link ZernovaStereotypes#OBJECT_STORE} stereotype if not already
 * present. If at least one parameter of that type is found, it also makes sure that {@link ObjectStore} is registered as an
 * imported type
 *
 * @since 4.1
 */
public class JavaObjectStoreParameterDeclarationEnricher implements WalkingDeclarationEnricher {

  @Override
  public Optional<DeclarationEnricherWalkDelegate> getWalkDelegate(ExtensionLoadingContext extensionLoadingContext) {
    return of(new IdempotentDeclarationEnricherWalkDelegate() {

      final ExtensionDeclaration extension = extensionLoadingContext.getExtensionDeclarer().getDeclaration();
      boolean hasObjectStoreParams = false;

      @Override
      protected void onParameter(ParameterGroupDeclaration parameterGroup, ParameterDeclaration parameter) {
        if (isObjectStore(parameter.getType())) {
          List<StereotypeModel> stereotypes = parameter.getAllowedStereotypeModels();
          if (!stereotypes.contains(OBJECT_STORE)) {
            stereotypes.add(OBJECT_STORE);
          }
          hasObjectStoreParams = true;
        }
      }

      @Override
      public void onWalkFinished() {
        if (hasObjectStoreParams && !isObjectStoreAlreadyImported(extension)) {
          ClassTypeLoader typeLoader = ExtensionsTypeLoaderFactory.getDefault().createTypeLoader();
          extension.getImportedTypes().add(new ImportedTypeModel((ObjectType) typeLoader.load(ObjectStore.class)));
        }
      }
    });
  }

  private boolean isObjectStoreAlreadyImported(ExtensionDeclaration extension) {
    return extension.getImportedTypes().stream().filter(model -> isObjectStore(model.getImportedType())).findAny().isPresent();
  }

  private boolean isObjectStore(MetadataType type) {
    return getTypeId(type)
        .filter(typeId -> ObjectStore.class.getName().equals(typeId))
        .isPresent();
  }
}
