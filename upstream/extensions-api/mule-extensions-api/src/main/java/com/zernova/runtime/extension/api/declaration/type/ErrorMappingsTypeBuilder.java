/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.declaration.type;

import static com.zernova.metadata.api.builder.BaseTypeBuilder.create;
import static com.zernova.metadata.api.model.MetadataFormat.JAVA;
import static com.zernova.runtime.extension.api.ExtensionConstants.ERROR_MAPPING_DESCRIPTION;
import static com.zernova.runtime.extension.api.error.ErrorConstants.ERROR_TYPE_DEFINITION;
import static com.zernova.runtime.extension.api.error.ErrorConstants.ERROR_TYPE_MATCHER;

import com.zernova.metadata.api.builder.ArrayTypeBuilder;
import com.zernova.metadata.api.builder.ObjectTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.extension.api.declaration.type.annotation.InfrastructureTypeAnnotation;
import com.zernova.runtime.extension.api.declaration.type.annotation.TypeDslAnnotation;

/**
 * Creates instances of {@link MetadataType} which represent an error mapping.
 *
 * @since 1.4
 */
public class ErrorMappingsTypeBuilder extends InfrastructureTypeBuilder {

  public MetadataType buildErrorMappingsType() {
    final ObjectTypeBuilder errorMappingType = create(JAVA).objectType().id("errorMapping");
    // 'source' is not required, not setting it means ANY
    errorMappingType.addField()
        .key("source")
        .value(ERROR_TYPE_MATCHER);
    errorMappingType.addField()
        .key("target")
        .required()
        .value(ERROR_TYPE_DEFINITION);
    errorMappingType.with(new InfrastructureTypeAnnotation());

    final ArrayTypeBuilder type = create(JAVA).arrayType()
        .of(errorMappingType)
        .with(new InfrastructureTypeAnnotation())
        .description(ERROR_MAPPING_DESCRIPTION)
        .with(new TypeDslAnnotation(true, false, null, null));

    return type.build();
  }
}
