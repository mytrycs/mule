/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.declaration.type.annotation;

import com.zernova.metadata.api.annotation.TypeAnnotation;
import com.zernova.metadata.api.model.ObjectFieldType;
import com.zernova.metadata.api.model.ObjectType;

/**
 * A marker {@link TypeAnnotation} meant to be applied on {@link ObjectFieldType} instances which
 * {@link ObjectFieldType#getValue()} points to an {@link ObjectType}. The presence of this annotation means that the fields in
 * such target object should be flattened into the owner {@link ObjectType}.
 *
 * @since 1.0
 */
public class FlattenedTypeAnnotation implements TypeAnnotation {

  public static final String NAME = "flattened";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public int hashCode() {
    return NAME.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof FlattenedTypeAnnotation;
  }
}
