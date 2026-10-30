/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.enricher;

import static com.zernova.metadata.api.utils.MetadataTypeUtils.getTypeId;
import static com.zernova.runtime.api.meta.model.connection.ConnectionManagementType.POOLING;
import static com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase.STRUCTURE;
import static com.zernova.runtime.extension.internal.loader.util.InfrastructureParameterBuilder.addPoolingProfileParameter;
import static com.zernova.runtime.extension.internal.loader.util.InfrastructureParameterBuilder.addReconnectionConfigParameter;
import static com.zernova.runtime.extension.privileged.util.ComponentDeclarationUtils.isReconnectionStrategySupported;

import static java.util.Optional.of;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.runtime.api.config.PoolingProfile;
import com.zernova.runtime.api.meta.model.ImportedTypeModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionManagementType;
import com.zernova.runtime.api.meta.model.declaration.fluent.ConnectionProviderDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.api.tls.TlsContextFactory;
import com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.IdempotentDeclarationEnricherWalkDelegate;
import com.zernova.runtime.extension.api.loader.WalkingDeclarationEnricher;

import java.util.Optional;

/**
 * Enriches all the {@link ConnectionProviderDeclaration} by adding language rules parameters.
 * <p>
 * In concrete it:
 * <p>
 * <ul>
 * <li>Add a reconnection strategy parameter</li>
 * <li>A {@link PoolingProfile} parameter when the {@link ConnectionManagementType} is
 * {@link ConnectionManagementType#POOLING}</li>
 * <li>A parameter which allows disabling connection validation when the {@link ConnectionManagementType} is
 * {@link ConnectionManagementType#POOLING} or {@link ConnectionManagementType#CACHED}</li>
 * </ul>
 *
 * @since 1.0
 */
public class ConnectionProviderDeclarationEnricher implements WalkingDeclarationEnricher {


  @Override
  public DeclarationEnricherPhase getExecutionPhase() {
    return STRUCTURE;
  }

  @Override
  public Optional<DeclarationEnricherWalkDelegate> getWalkDelegate(ExtensionLoadingContext extensionLoadingContext) {
    final ExtensionDeclarer extensionDeclarer = extensionLoadingContext.getExtensionDeclarer();
    final ExtensionDeclaration extDeclaration = extensionDeclarer.getDeclaration();

    return of(new IdempotentDeclarationEnricherWalkDelegate() {

      boolean thereAreConnectionProviders = false;

      @Override
      protected void onConnectionProvider(ConnectionProviderDeclaration declaration) {
        if (isReconnectionStrategySupported(extDeclaration)) {
          addReconnectionConfigParameter(declaration);
        }
        ConnectionManagementType managementType = declaration.getConnectionManagementType();

        if (managementType == POOLING) {
          addPoolingProfileParameter(declaration);
        }

        thereAreConnectionProviders = true;
      }

      @Override
      public void onWalkFinished() {
        if (thereAreConnectionProviders && !isTlsContextFactoryAlreadyImported(extDeclaration)) {
          extensionDeclarer.withImportedType(new ImportedTypeModel((ObjectType) loadTlsContextFactoryType(extensionLoadingContext
              .getTypeLoader())));
        }
      }
    });
  }

  private MetadataType loadTlsContextFactoryType(ClassTypeLoader loader) {
    return loader.load(TlsContextFactory.class);
  }

  private boolean isTlsContextFactoryAlreadyImported(ExtensionDeclaration extension) {
    return extension.getImportedTypes().stream().anyMatch(model -> isTlsContextFactory(model.getImportedType()));
  }

  private boolean isTlsContextFactory(MetadataType type) {
    return getTypeId(type)
        .filter(typeId -> TlsContextFactory.class.getName().equals(typeId))
        .isPresent();
  }

}
