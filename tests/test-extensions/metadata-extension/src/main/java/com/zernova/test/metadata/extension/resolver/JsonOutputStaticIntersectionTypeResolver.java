/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension.resolver;

import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.builder.IntersectionTypeBuilder;
import com.zernova.metadata.api.builder.ObjectTypeBuilder;
import com.zernova.metadata.api.model.MetadataFormat;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.metadata.resolving.OutputStaticTypeResolver;

public class JsonOutputStaticIntersectionTypeResolver extends OutputStaticTypeResolver {

  @Override
  public MetadataType getStaticMetadata() {
    ObjectTypeBuilder object = BaseTypeBuilder.create(MetadataFormat.JSON).objectType();
    object.id("json-object1");
    object.addField().key("age").value().numberType();
    object.addField().key("dni").value().numberType();

    IntersectionTypeBuilder intersectionTypeBuilder = BaseTypeBuilder.create(MetadataFormat.JSON).intersectionType();
    intersectionTypeBuilder.of(object);
    return intersectionTypeBuilder.build();
  }
}
