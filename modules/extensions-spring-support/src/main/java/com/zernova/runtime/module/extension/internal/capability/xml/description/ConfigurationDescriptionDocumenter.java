/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.capability.xml.description;

import static com.zernova.runtime.extension.api.util.NameUtils.hyphenize;
import static com.zernova.runtime.module.extension.internal.ExtensionProperties.DEFAULT_CONNECTION_PROVIDER_NAME;
import com.zernova.runtime.api.meta.model.declaration.fluent.ConfigurationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ConnectedDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ConnectionProviderDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;

import java.util.List;
import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * {@link AbstractDescriptionDocumenter} implementation that fills {@link ConfigurationDeclaration}s
 *
 * @since 4.0
 */
final class ConfigurationDescriptionDocumenter extends AbstractDescriptionDocumenter {

  private final ParameterDescriptionDocumenter parameterDeclarer;
  private final OperationDescriptionDocumenter operationDeclarer;
  private final SourcesDescriptionDocumenter sourceDeclarer;

  ConfigurationDescriptionDocumenter(ProcessingEnvironment processingEnv) {
    super(processingEnv);
    this.parameterDeclarer = new ParameterDescriptionDocumenter(processingEnv);
    this.operationDeclarer = new OperationDescriptionDocumenter(processingEnv);
    this.sourceDeclarer = new SourcesDescriptionDocumenter(processingEnv);
  }

  void document(ExtensionDeclaration extensionDeclaration, ConfigurationDeclaration declaration, TypeElement configElement) {
    declaration.setDescription(processor.getJavaDocSummary(processingEnv, configElement));
    operationDeclarer.document(configElement, declaration, extensionDeclaration);
    sourceDeclarer.document(configElement, declaration, extensionDeclaration);
    documentConnectionProviders(declaration, configElement);
    parameterDeclarer.document(declaration, configElement);
  }

  public void documentConnectionProviders(ConnectedDeclaration<?> declaration, TypeElement element) {
    getConnectionProviderClasses(processingEnv, element)
        .forEach(providerElement -> findMatchingProvider(declaration, providerElement)
            .ifPresent(provider -> {
              provider.setDescription(processor.getJavaDocSummary(processingEnv, providerElement));
              parameterDeclarer.document(provider, providerElement);
            }));
  }

  private Optional<ConnectionProviderDeclaration> findMatchingProvider(ConnectedDeclaration<?> declaration, Element element) {
    Optional<String> alias = getAlias(element);
    String defaultNaming = hyphenize(element.getSimpleName().toString().replace("Provider", ""));
    return declaration.getConnectionProviders().stream()
        .filter(provider -> {
          String name = provider.getName();
          if (alias.isPresent()) {
            return name.equals(alias.get());
          } else {
            return name.equals(defaultNaming) || name.equals(DEFAULT_CONNECTION_PROVIDER_NAME);
          }
        })
        .findAny();
  }

  private List<TypeElement> getConnectionProviderClasses(ProcessingEnvironment processingEnv, TypeElement element) {
    return processor.getArrayClassAnnotationValue(element, ConnectionProviders.class, VALUE_PROPERTY, processingEnv);
  }
}
