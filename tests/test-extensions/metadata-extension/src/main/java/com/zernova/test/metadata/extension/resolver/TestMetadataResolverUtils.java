/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension.resolver;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static com.zernova.metadata.api.model.MetadataFormat.JAVA;
import static com.zernova.runtime.api.metadata.resolving.FailureCode.INVALID_METADATA_KEY;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.builder.ObjectTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.MetadataKeyBuilder;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.test.metadata.extension.MetadataConnection;

import java.util.Set;

public class TestMetadataResolverUtils {

  public static final String TIRES = "Tires";
  public static final String DIRECTION = "Direction";
  public static final String NAME = "Name";
  public static final String APPLICATION_JAVA_MIME_TYPE = "application/java";
  public static final String BRAND = "Brand";
  public static final String SIZE = "Size";
  public static final String AGE = "Age";

  public static Set<MetadataKey> getKeys(MetadataContext context) throws ConnectionException {
    return context.getConnection().map(c -> {
      MetadataConnection connection = (MetadataConnection) c;
      return connection.getEntities().stream().map(e -> MetadataKeyBuilder.newKey(e).build()).collect(toSet());
    }).orElse(emptySet());
  }

  public static Set<com.zernova.sdk.api.metadata.MetadataKey> getSdkKeys(com.zernova.sdk.api.metadata.MetadataContext context)
      throws ConnectionException {
    return context.getConnection().map(c -> {
      MetadataConnection connection = (MetadataConnection) c;
      return connection.getEntities().stream().map(e -> com.zernova.sdk.api.metadata.MetadataKeyBuilder.newKey(e).build())
          .collect(toSet());
    }).orElse(emptySet());
  }

  public static MetadataType getMetadata(String key) throws MetadataResolvingException {


    switch (key) {
      case MetadataConnection.CAR:
        return getCarMetadata();
      case MetadataConnection.HOUSE:
        return getHouseMetadata();
      case MetadataConnection.PERSON:
        return getPersonMetadata();
      case MetadataConnection.NULL:
        return BaseTypeBuilder.create(JAVA).nullType().build();
      case MetadataConnection.VOID:
        return BaseTypeBuilder.create(JAVA).voidType().build();
      default:
        throw new MetadataResolvingException("Unknown key " + key, INVALID_METADATA_KEY);
    }
  }

  public static MetadataType getCarMetadata() {
    ObjectTypeBuilder objectBuilder = BaseTypeBuilder.create(JAVA).objectType();
    objectBuilder.addField().key(TIRES).value().numberType();
    objectBuilder.addField().key(BRAND).value().stringType();
    return objectBuilder.build();
  }

  public static MetadataType getHouseMetadata() {
    ObjectTypeBuilder objectBuilder = BaseTypeBuilder.create(JAVA).objectType();
    objectBuilder.addField().key(DIRECTION).value().stringType();
    objectBuilder.addField().key(SIZE).value().numberType();
    return objectBuilder.build();
  }

  public static MetadataType getPersonMetadata() {
    ObjectTypeBuilder objectBuilder = BaseTypeBuilder.create(JAVA).objectType();
    objectBuilder.addField().key(NAME).value().stringType();
    objectBuilder.addField().key(AGE).value().numberType();
    return objectBuilder.build();
  }

}
