/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.internal.meta.model;


import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.TypeAnnotationModelPropertyWrapper;

/**
 * Default implementation of {@link TypeAnnotationModelPropertyWrapper}.
 */
public class DefaultTypeAnnotationModelPropertyWrapper implements TypeAnnotationModelPropertyWrapper {

  private final ModelProperty modelProperty;

  public DefaultTypeAnnotationModelPropertyWrapper(ModelProperty modelProperty) {
    this.modelProperty = modelProperty;
  }

  @Override
  public ModelProperty asModelProperty() {
    return modelProperty;
  }

  @Override
  public String getName() {
    return modelProperty.getName();
  }

  @Override
  public boolean isPublic() {
    return false;
  }
}
