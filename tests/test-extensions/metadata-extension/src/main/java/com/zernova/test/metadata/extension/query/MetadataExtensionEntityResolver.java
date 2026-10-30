/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension.query;

import static java.util.stream.Collectors.toSet;
import static com.zernova.runtime.api.metadata.MetadataKeyBuilder.newKey;
import static com.zernova.runtime.api.metadata.resolving.FailureCode.INVALID_METADATA_KEY;
import static com.zernova.test.metadata.extension.query.NativeQueryOutputResolver.QUERY_RESOLVER_CATEGORY;
import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.resolving.QueryEntityResolver;
import com.zernova.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import com.zernova.test.metadata.extension.model.shapes.Circle;
import com.zernova.test.metadata.extension.model.shapes.Square;

import java.util.Set;
import java.util.stream.Stream;

public class MetadataExtensionEntityResolver implements QueryEntityResolver {

  private static final ClassTypeLoader LOADER = ExtensionsTypeLoaderFactory.getDefault().createTypeLoader();

  public static final String CIRCLE = "Circle";
  public static final String SQUARE = "Square";

  @Override
  public Set<MetadataKey> getEntityKeys(MetadataContext context) throws MetadataResolvingException, ConnectionException {
    return Stream.of(newKey(SQUARE).build(), newKey(CIRCLE).build()).collect(toSet());
  }

  @Override
  public MetadataType getEntityMetadata(MetadataContext context, String key)
      throws MetadataResolvingException, ConnectionException {
    switch (key) {
      case CIRCLE:
        return LOADER.load(Circle.class);
      case SQUARE:
        return LOADER.load(Square.class);
      default:
        throw new MetadataResolvingException("Invalid metadata key: " + key, INVALID_METADATA_KEY);
    }
  }

  @Override
  public String getCategoryName() {
    return QUERY_RESOLVER_CATEGORY;
  }
}
