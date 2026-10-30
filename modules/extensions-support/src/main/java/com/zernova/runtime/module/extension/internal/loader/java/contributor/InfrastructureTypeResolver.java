/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.contributor;

import static com.zernova.runtime.api.util.collection.Collectors.toImmutableMap;
import static com.zernova.runtime.extension.api.loader.util.InfrastructureTypeUtils.getActionableInfrastructureTypes;

import static java.util.function.Function.identity;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.extension.api.declaration.type.DefaultExtensionsTypeLoaderFactory;
import com.zernova.runtime.extension.api.loader.util.InfrastructureTypeUtils;
import com.zernova.runtime.extension.api.loader.util.InfrastructureTypeUtils.MetadataTypeBasedInfrastructureType;
import com.zernova.runtime.module.extension.api.loader.java.type.Type;
import com.zernova.runtime.module.extension.internal.loader.java.type.runtime.TypeWrapper;

import java.util.Map;
import java.util.Optional;

/**
 * Resolves whether a {@link Type} is one of the considered as an Infrastructure Type.
 *
 * @since 4.4
 */
public class InfrastructureTypeResolver {

  private static final ClassTypeLoader TYPE_LOADER = new DefaultExtensionsTypeLoaderFactory()
      .createTypeLoader(InfrastructureTypeUtils.class.getClassLoader());

  private static final Map<Type, MetadataTypeBasedInfrastructureType> TYPE_MAPPING = getActionableInfrastructureTypes()
      .stream()
      .collect(toImmutableMap(infrastructureType -> new TypeWrapper(infrastructureType.getClazz(), TYPE_LOADER),
                              identity()));


  public static Optional<MetadataTypeBasedInfrastructureType> getInfrastructureType(Type type) {
    return TYPE_MAPPING.entrySet()
        .stream()
        .filter(entry -> entry.getKey().isSameType(type))
        .map(Map.Entry::getValue)
        .findFirst();
  }
}

