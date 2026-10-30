/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.test.meta.model.tck;

import static com.zernova.metadata.api.model.MetadataFormat.JAVA;

import com.zernova.metadata.api.annotation.TypeIdAnnotation;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.builder.WithAnnotation;
import com.zernova.metadata.api.model.BinaryType;
import com.zernova.metadata.api.model.BooleanType;
import com.zernova.metadata.api.model.NumberType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.metadata.api.model.StringType;
import com.zernova.metadata.api.model.VoidType;

import java.io.InputStream;

public abstract class TestBaseDeclarer {

  protected final BaseTypeBuilder typeBuilder = BaseTypeBuilder.create(JAVA);

  protected StringType getStringType() {
    return withType(typeBuilder.stringType(), String.class).build();
  }

  protected NumberType getNumberType() {
    return withType(typeBuilder.numberType(), Integer.class).build();
  }

  protected BinaryType getBinaryType() {
    return withType(typeBuilder.binaryType(), InputStream.class).build();
  }

  protected BooleanType getBooleanType() {
    return withType(typeBuilder.booleanType(), Boolean.class).build();
  }

  protected ObjectType getObjectType(Class<?> type) {
    return withType(typeBuilder.objectType(), type).build();
  }

  protected VoidType getVoidType() {
    return typeBuilder.voidType().build();
  }

  protected <T extends WithAnnotation<?>> T withType(T builder, Class<?> type) {
    return (T) builder.with(new TypeIdAnnotation(type.getName()));
  }
}
