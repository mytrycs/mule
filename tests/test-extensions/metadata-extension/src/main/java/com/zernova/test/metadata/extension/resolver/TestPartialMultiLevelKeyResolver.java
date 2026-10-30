/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension.resolver;

import static com.zernova.runtime.api.metadata.MetadataKeyBuilder.newKey;
import static com.zernova.test.metadata.extension.resolver.TestMetadataResolverUtils.APPLICATION_JAVA_MIME_TYPE;

import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.builder.ObjectTypeBuilder;
import com.zernova.metadata.api.model.MetadataFormat;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.resolving.FailureCode;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.PartialTypeKeysResolver;
import com.zernova.test.metadata.extension.LocationKey;

public class TestPartialMultiLevelKeyResolver extends TestMultiLevelKeyResolver
    implements PartialTypeKeysResolver<LocationKey>, OutputTypeResolver<LocationKey> {

  @Override
  public MetadataKey resolveChilds(MetadataContext context, LocationKey partial)
      throws MetadataResolvingException, ConnectionException {

    // This is incomplete but you get the idea
    if (AMERICA.equalsIgnoreCase(partial.getContinent())) {

      if (ARGENTINA.equalsIgnoreCase(partial.getCountry())) {
        return newKey(AMERICA).withDisplayName(AMERICA)
            .withChild(newKey(ARGENTINA)
                .withChild(newKey(BUENOS_AIRES))
                .withChild(newKey(LA_PLATA)))
            .build();
      }

      return buildAmericaKey();

    } else if (EUROPE.equalsIgnoreCase(partial.getContinent())) {
      return buildEuropeKey();

    } else {
      throw new MetadataResolvingException("Invalid Continent", FailureCode.INVALID_METADATA_KEY);
    }
  }

  public static MetadataKey buildEuropeKey() {
    return newKey(EUROPE).withDisplayName(EUROPE).withChild(newKey(FRANCE).withChild(newKey(PARIS))).build();
  }

  public static MetadataKey buildAmericaKey() {
    return newKey(AMERICA).withDisplayName(AMERICA)
        .withChild(newKey(ARGENTINA).withChild(newKey(BUENOS_AIRES)).withChild(newKey(LA_PLATA)))
        .withChild(newKey(USA).withDisplayName(USA_DISPLAY_NAME).withChild(newKey(SAN_FRANCISCO))).build();
  }

  @Override
  public String getResolverName() {
    return "PartialTestMultiLevelKeyResolver";
  }

  @Override
  public MetadataType getOutputType(MetadataContext context, LocationKey key)
      throws MetadataResolvingException, ConnectionException {
    final ObjectTypeBuilder objectBuilder =
        BaseTypeBuilder.create(new MetadataFormat(key.toString(), key.toString(), APPLICATION_JAVA_MIME_TYPE)).objectType();
    objectBuilder.addField().key(key.getContinent()).value().stringType();
    objectBuilder.addField().key(key.getCity()).value().stringType();
    objectBuilder.addField().key(key.getCountry()).value().stringType();
    return objectBuilder.build();
  }

}
