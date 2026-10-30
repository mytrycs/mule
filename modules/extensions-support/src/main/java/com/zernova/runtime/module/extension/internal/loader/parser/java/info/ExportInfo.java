/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.info;

import com.zernova.runtime.extension.api.annotation.Export;
import com.zernova.runtime.module.extension.api.loader.java.type.AnnotationValueFetcher;
import com.zernova.runtime.module.extension.api.loader.java.type.Type;

import java.util.List;

/**
 * Simple bean which maps the info defined in the {@link com.zernova.runtime.extension.api.annotation.Export} or
 * {@link com.zernova.sdk.api.annotation.Export} annotations, so that consumers can decouple from which was used.
 *
 * @since 4.5.0
 */
public class ExportInfo {

  private final List<Type> types;
  private final List<String> resources;

  public static ExportInfo fromLegacy(AnnotationValueFetcher<Export> annotation) {
    return new ExportInfo(annotation.getClassArrayValue(Export::classes), annotation.getArrayValue(Export::resources));
  }

  public static ExportInfo fromSdkApi(AnnotationValueFetcher<com.zernova.sdk.api.annotation.Export> annotation) {
    return new ExportInfo(annotation.getClassArrayValue(com.zernova.sdk.api.annotation.Export::classes),
                          annotation.getArrayValue(com.zernova.sdk.api.annotation.Export::resources));
  }

  public ExportInfo(List<Type> types, List<String> resources) {
    this.types = types;
    this.resources = resources;
  }

  public List<Type> getExportedTypes() {
    return types;
  }

  public List<String> getExportedResources() {
    return resources;
  }
}
