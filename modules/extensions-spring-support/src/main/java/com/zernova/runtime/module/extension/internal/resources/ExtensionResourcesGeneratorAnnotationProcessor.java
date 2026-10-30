/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.resources;

import static javax.lang.model.SourceVersion.RELEASE_8;
import static com.zernova.runtime.module.extension.internal.resources.BaseExtensionResourcesGeneratorAnnotationProcessor.EXTENSION_VERSION;

import com.zernova.runtime.extension.api.loader.ExtensionModelLoadingRequest;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;
import com.zernova.runtime.module.extension.internal.capability.xml.description.DescriptionDeclarationEnricher;
import com.zernova.runtime.module.extension.internal.resources.validator.ExportedPackagesValidator;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;


/**
 * {@link BaseExtensionResourcesGeneratorAnnotationProcessor} implementation for default java based extensions.
 *
 * @since 3.7.0
 * @deprecated Use com.zernova.runtime.module.extension.api.ExtensionResourcesGeneratorAnnotationProcessor instead.
 */
@SupportedAnnotationTypes(value = {"com.zernova.runtime.extension.api.annotation.Extension"})
@SupportedSourceVersion(RELEASE_8)
@SupportedOptions(EXTENSION_VERSION)
@Deprecated
public class ExtensionResourcesGeneratorAnnotationProcessor
    extends com.zernova.runtime.module.extension.api.resources.ExtensionResourcesGeneratorAnnotationProcessor {

}
