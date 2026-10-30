/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.resources;

import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.resources.ResourcesGenerator;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

/**
 * Annotation processor that picks up all the extensions annotated with {@link Extension} or
 * {@link com.zernova.sdk.api.annotation.Extension} and uses a {@link ResourcesGenerator} to generate the required resources.
 * <p>
 * This annotation processor will automatically generate and package into the output jar the XSD schema, spring bundles and
 * extension registration files necessary for mule to work with this extension.
 * <p>
 * Depending on the model properties declared by each extension, some of those resources might or might not be generated
 *
 * @since 3.7.0
 * @deprecated Use com.zernova.runtime.module.extension.api.resources.BaseExtensionResourcesGeneratorAnnotationProcessor instead.
 */
@SupportedAnnotationTypes(value = {"com.zernova.runtime.extension.api.annotation.Extension"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(BaseExtensionResourcesGeneratorAnnotationProcessor.EXTENSION_VERSION)
@Deprecated
public abstract class BaseExtensionResourcesGeneratorAnnotationProcessor
    extends com.zernova.runtime.module.extension.api.resources.BaseExtensionResourcesGeneratorAnnotationProcessor {

}
