/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension.resolver;

import static java.util.Collections.emptySet;
import static com.zernova.metadata.api.model.MetadataFormat.JAVA;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.MetadataKeyBuilder;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.resolving.AttributesTypeResolver;
import com.zernova.runtime.api.metadata.resolving.InputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.QueryEntityResolver;
import com.zernova.runtime.api.metadata.resolving.TypeKeysResolver;
import com.zernova.runtime.extension.api.metadata.NullMetadataKey;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class TestNoConfigMetadataResolver implements TypeKeysResolver, InputTypeResolver<Object>,
    OutputTypeResolver<Object>, AttributesTypeResolver<Object>, QueryEntityResolver {

  @Override
  public String getCategoryName() {
    return "MetadataExtensionResolver";
  }

  @Override
  public String getResolverName() {
    return "TestNoConfigMetadataResolver";
  }

  @Override
  public Set<MetadataKey> getEntityKeys(MetadataContext context) throws MetadataResolvingException, ConnectionException {
    return emptySet();
  }

  @Override
  public MetadataType getEntityMetadata(MetadataContext context, String key)
      throws MetadataResolvingException, ConnectionException {
    return BaseTypeBuilder.create(JAVA).voidType().build();
  }

  @Override
  public Set<MetadataKey> getKeys(MetadataContext context) {
    return Arrays.stream(KeyIds.values()).map(e -> MetadataKeyBuilder.newKey(e.name()).build()).collect(Collectors.toSet());
  }

  @Override
  public MetadataType getInputMetadata(MetadataContext context, Object key) {
    if (key instanceof NullMetadataKey) {
      return BaseTypeBuilder.create(JAVA).voidType().build();
    }

    return BaseTypeBuilder.create(JAVA).stringType().build();
  }

  @Override
  public MetadataType getOutputType(MetadataContext context, Object key) {
    if (key instanceof NullMetadataKey) {
      return BaseTypeBuilder.create(JAVA).voidType().build();
    }

    return BaseTypeBuilder.create(JAVA).booleanType().build();
  }

  @Override
  public MetadataType getAttributesType(MetadataContext context, Object key)
      throws MetadataResolvingException, ConnectionException {
    if (key instanceof NullMetadataKey) {
      return BaseTypeBuilder.create(JAVA).voidType().build();
    }

    return BaseTypeBuilder.create(JAVA).booleanType().build();
  }

  public enum KeyIds {
    BOOLEAN, STRING
  }
}
