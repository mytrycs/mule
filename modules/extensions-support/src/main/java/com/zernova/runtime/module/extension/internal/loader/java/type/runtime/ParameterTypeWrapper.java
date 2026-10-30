/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.type.runtime;

import static com.zernova.metadata.api.model.MetadataFormat.JAVA;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.module.extension.internal.loader.enricher.MetadataTypeEnricher;

import java.io.InputStream;
import java.io.Serializable;

import org.springframework.core.ResolvableType;

/**
 * Wrapper for {@link Class} of parameter method that provide utility methods to facilitate the introspection of the {@link Class}
 *
 * @since 4.2
 */
public class ParameterTypeWrapper extends TypeWrapper {

  public ParameterTypeWrapper(Class<?> aClass, ClassTypeLoader typeLoader) {
    super(aClass, typeLoader);
  }

  public ParameterTypeWrapper(ResolvableType resolvableType, ClassTypeLoader typeLoader) {
    super(resolvableType, typeLoader);
  }

  @Override
  public MetadataType asMetadataType() {
    MetadataType metadataType = typeLoader.load(type);

    if (this.isSameType(Object.class) ||
        this.isSameType(Serializable.class) ||
        this.isAssignableTo(InputStream.class) ||
        this.isAssignableTo(Byte[].class) ||
        this.isAssignableTo(byte[].class)) {

      MetadataTypeEnricher enricher = new MetadataTypeEnricher();
      return enricher.enrich(BaseTypeBuilder.create(JAVA).anyType().build(), typeLoader.load(type).getAnnotations());
    }

    return metadataType;
  }

}
