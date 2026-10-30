/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.resources;

import static com.zernova.runtime.module.artifact.activation.api.extension.discovery.boot.ExtensionLoaderUtils.getLoaderById;
import static com.zernova.runtime.module.extension.api.resources.BaseExtensionResourcesGeneratorAnnotationProcessor.EXTENSION_CLASSES;
import static com.zernova.runtime.module.extension.api.resources.BaseExtensionResourcesGeneratorAnnotationProcessor.EXTENSION_VERSION;

import static javax.lang.model.SourceVersion.RELEASE_17;

import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoadingRequest;
import com.zernova.runtime.module.extension.api.resources.ClassExtensionResourcesGeneratorAnnotationProcessor;
import com.zernova.runtime.module.extension.internal.capability.xml.description.DescriptionDeclarationEnricher;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;

/**
 * {@link BaseExtensionResourcesGeneratorAnnotationProcessor} implementation for SOAP BASED Extensions that use the soap
 * extensions api classes and annotations.
 *
 * @since 4.0.0
 */
@SupportedAnnotationTypes(value = {"com.zernova.runtime.extension.api.annotation.Extension"})
@SupportedSourceVersion(RELEASE_17)
@SupportedOptions({
    EXTENSION_VERSION,
    EXTENSION_CLASSES
})
public class SoapExtensionResourcesGeneratorAnnotationProcessor extends ClassExtensionResourcesGeneratorAnnotationProcessor {

  @Override
  protected void configureLoadingRequest(ExtensionModelLoadingRequest.Builder requestBuilder) {
    super.configureLoadingRequest(requestBuilder);
    requestBuilder.addEnricher(new DescriptionDeclarationEnricher());
  }

  @Override
  protected ExtensionModelLoader getExtensionModelLoader() {
    return getLoaderById("soap");
  }
}
