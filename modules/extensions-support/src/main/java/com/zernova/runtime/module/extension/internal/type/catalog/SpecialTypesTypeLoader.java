/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.type.catalog;

import static com.zernova.metadata.api.builder.BaseTypeBuilder.create;
import static com.zernova.metadata.api.model.MetadataFormat.JAVA;
import static com.zernova.runtime.api.util.collection.SmallMap.of;

import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

import com.zernova.metadata.api.TypeLoader;
import com.zernova.metadata.api.model.MetadataType;

import java.util.Map;
import java.util.Optional;

/**
 * a {@link TypeLoader} for obtaining special types that aren't primitive types (such as string, number, etc.) nor defined in
 * other extensions.
 *
 * @since 4.5.0
 */
public class SpecialTypesTypeLoader implements TypeLoader {

  public static final String VOID = "void";
  public static final String ERROR = "error";

  private static final Map<String, MetadataType> SPECIAL_TYPES =
      unmodifiableMap(of(VOID, create(JAVA).voidType().build(), ERROR,
                         com.zernova.runtime.extension.api.error.ErrorConstants.ERROR));

  @Override
  public Optional<MetadataType> load(String typeIdentifier) {
    return ofNullable(SPECIAL_TYPES.get(typeIdentifier));
  }
}
