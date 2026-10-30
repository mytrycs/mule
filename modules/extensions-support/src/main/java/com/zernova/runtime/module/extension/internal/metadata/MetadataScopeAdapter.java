/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.metadata;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.metadata.resolving.AttributesTypeResolver;
import com.zernova.runtime.api.metadata.resolving.InputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.TypeKeysResolver;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataScope;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Adapter implementation which expands the {@link MetadataScope} to a more descriptive of the developer's metadata declaration
 * for a {@link ComponentModel component}
 *
 * @since 4.0
 */
public interface MetadataScopeAdapter {

  default boolean isCustomScope() {
    return hasOutputResolver() || hasInputResolvers();
  }

  boolean isPartialKeyResolver();

  boolean hasKeysResolver();

  boolean hasInputResolvers();

  boolean hasOutputResolver();

  boolean hasAttributesResolver();

  TypeKeysResolver getKeysResolver();

  Map<String, Supplier<? extends InputTypeResolver>> getInputResolvers();

  OutputTypeResolver getOutputResolver();

  AttributesTypeResolver getAttributesResolver();

  MetadataType getKeyResolverMetadataType();

  String getKeyResolverParameterName();
}
