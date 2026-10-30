/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.capability.xml.description;

import static com.zernova.runtime.module.extension.api.resources.BaseExtensionResourcesGeneratorAnnotationProcessor.EXTENSION_ELEMENT;
import static com.zernova.runtime.module.extension.api.resources.BaseExtensionResourcesGeneratorAnnotationProcessor.PROCESSING_ENVIRONMENT;
import static com.zernova.runtime.module.extension.api.resources.BaseExtensionResourcesGeneratorAnnotationProcessor.ROUND_ENVIRONMENT;

import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Implementation of {@link DeclarationEnricher} that fills the descriptions for all the components in the
 * {@link ExtensionDeclaration} that is being declared.
 * <p>
 * When the source code is available uses the APT to access the AST tree and extract the extensions javadocs which are used to
 * enrich the extension's descriptions.
 * <p/>
 * For this to be possible, the context should have as custom parameters a {@link ProcessingEnvironment} and the corresponding
 * {@link TypeElement}, which will be fetched in the provided context under the keys
 * {@link BaseExtensionResourcesGeneratorAnnotationProcessor#PROCESSING_ENVIRONMENT} and
 * {@link BaseExtensionResourcesGeneratorAnnotationProcessor#EXTENSION_ELEMENT}.
 *
 * @since 4.0
 */
public final class DescriptionDeclarationEnricher implements DeclarationEnricher {

  @Override
  public void enrich(ExtensionLoadingContext loadingContext) {
    ProcessingEnvironment processingEnv = getParameterOrFail(loadingContext, PROCESSING_ENVIRONMENT);
    TypeElement extensionElement = getParameterOrFail(loadingContext, EXTENSION_ELEMENT);
    RoundEnvironment roundEnvironment = getParameterOrFail(loadingContext, ROUND_ENVIRONMENT);
    ExtensionDescriptionDocumenter declarer = new ExtensionDescriptionDocumenter(processingEnv, roundEnvironment);
    declarer.document(loadingContext.getExtensionDeclarer().getDeclaration(), extensionElement);
  }

  private <T> T getParameterOrFail(ExtensionLoadingContext loadingContext, String key) {
    return loadingContext.<T>getParameter(key)
        .orElseThrow(() -> new IllegalStateException("Couldn't obtain [" + key + "] from the Extension Loading Context"));
  }
}
