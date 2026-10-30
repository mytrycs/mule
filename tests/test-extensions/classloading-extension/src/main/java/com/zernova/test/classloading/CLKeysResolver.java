/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.classloading;

import static com.zernova.metadata.api.model.MetadataFormat.JAVA;
import static com.zernova.test.classloading.api.ClassLoadingHelper.addClassLoader;

import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.MetadataKeyBuilder;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.TypeKeysResolver;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class CLKeysResolver implements TypeKeysResolver, OutputTypeResolver {

  public static final String GET_METADATA = "Metadata";

  @Override
  public Set<MetadataKey> getKeys(MetadataContext context) throws MetadataResolvingException, ConnectionException {
    addClassLoader(GET_METADATA);
    return ImmutableSet.of(MetadataKeyBuilder.newKey("VALUE").build());
  }

  @Override
  public String getResolverName() {
    return "ClassLoadingResolver";
  }

  @Override
  public String getCategoryName() {
    return "ClassLoadingCategory";
  }

  @Override
  public MetadataType getOutputType(MetadataContext context, Object key) throws MetadataResolvingException, ConnectionException {
    return BaseTypeBuilder.create(JAVA).anyType().build();
  }
}
