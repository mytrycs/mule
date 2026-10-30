/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.persistence.metadata;

import com.zernova.runtime.extension.internal.persistence.ComponentMetadataTypesDescriptorResult;
import com.zernova.runtime.extension.internal.persistence.ComponentMetadataTypesDescriptorResultTypeAdapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/**
 * {@link TypeAdapterFactory}
 *
 * @since 1.4
 */
public final class ComponentMetadataTypesTypeAdapterFactory
    implements TypeAdapterFactory {

  @Override
  public <C> TypeAdapter<C> create(Gson gson, TypeToken<C> type) {
    return type.getRawType().isAssignableFrom(ComponentMetadataTypesDescriptorResult.class)
        ? (TypeAdapter<C>) new ComponentMetadataTypesDescriptorResultTypeAdapter(gson)
        : null;
  }
}
