/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.declaration.type;

import com.zernova.metadata.api.annotation.TypeAliasAnnotation;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.builder.TypeBuilder;
import com.zernova.metadata.java.api.annotation.ClassInformationAnnotation;
import com.zernova.metadata.java.api.handler.ClassHandler;
import com.zernova.metadata.java.api.handler.TypeHandlerManager;
import com.zernova.metadata.java.api.utils.ParsingContext;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.extension.api.declaration.type.annotation.ExtensibleTypeAnnotation;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Implementation of {@link ClassHandler} for the {@link ObjectStore} interface
 *
 * @since 1.1
 */
public class ObjectStoreClassHandler implements ClassHandler {

  @Override
  public boolean handles(Class<?> clazz) {
    return ObjectStore.class.equals(clazz);
  }

  @Override
  public TypeBuilder<?> handleClass(Class<?> clazz, List<Type> genericTypes, TypeHandlerManager typeHandlerManager,
                                    ParsingContext context, BaseTypeBuilder typeBuilder) {

    return typeBuilder.objectType().id(ObjectStore.class.getName())
        .with(new ExtensibleTypeAnnotation())
        .with(new TypeAliasAnnotation(ObjectStore.class.getSimpleName()))
        .with(new ClassInformationAnnotation(ObjectStore.class));
  }
}
