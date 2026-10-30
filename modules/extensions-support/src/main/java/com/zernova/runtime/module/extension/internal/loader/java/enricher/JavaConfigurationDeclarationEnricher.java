/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.enricher;

import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.extractImplementingTypeProperty;

import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.declaration.fluent.BaseDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.SourceDeclaration;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.IdempotentDeclarationEnricherWalkDelegate;
import com.zernova.runtime.extension.api.loader.WalkingDeclarationEnricher;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionParameter;
import com.zernova.runtime.module.extension.api.loader.java.type.SourceElement;
import com.zernova.runtime.module.extension.api.loader.java.type.WithParameters;
import com.zernova.runtime.module.extension.internal.loader.java.property.ConfigTypeModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.property.ConnectivityModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.type.property.ExtensionOperationDescriptorModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.type.property.ExtensionTypeDescriptorModelProperty;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * {@link DeclarationEnricher} implementation that walks through a {@link ExtensionDeclaration} and looks for annotated component
 * parameters (Sources and Operations), with {@link Connection} and adds a {@link ConnectivityModelProperty} or annotated with
 * {@link Config} and adds a {@link ConfigTypeModelProperty}
 *
 * @since 4.0
 */
public class JavaConfigurationDeclarationEnricher implements WalkingDeclarationEnricher {

  @Override
  public Optional<DeclarationEnricherWalkDelegate> getWalkDelegate(ExtensionLoadingContext extensionLoadingContext) {
    return extractImplementingTypeProperty(extensionLoadingContext.getExtensionDeclarer().getDeclaration())
        .map(p -> new IdempotentDeclarationEnricherWalkDelegate() {

          @Override
          protected void onOperation(OperationDeclaration declaration) {
            enrich(declaration, ExtensionOperationDescriptorModelProperty.class,
                   (operation, property) -> enrich(operation, property.getOperationElement()));
          }

          @Override
          public void onSource(SourceDeclaration declaration) {
            enrich(declaration, ExtensionTypeDescriptorModelProperty.class,
                   (source, property) -> enrich(source, (SourceElement) property.getType()));
          }
        });
  }

  private <P extends ModelProperty> void enrich(BaseDeclaration declaration, Class<P> propertyType,
                                                BiConsumer<BaseDeclaration, P> consumer) {
    declaration.getModelProperty(propertyType).ifPresent(p -> consumer.accept(declaration, (P) p));
  }

  private void enrich(BaseDeclaration declaration, WithParameters methodWrapper) {
    final List<ExtensionParameter> configParameters = methodWrapper.getParametersAnnotatedWith(Config.class);
    if (!configParameters.isEmpty()) {
      configParameters.get(0).getType().getDeclaringClass()
          .ifPresent(declaringClass -> declaration.addModelProperty(new ConfigTypeModelProperty(declaringClass)));

    }
  }
}
