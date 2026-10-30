/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model;

import com.zernova.metadata.api.annotation.TypeAnnotation;
import com.zernova.runtime.internal.meta.model.DefaultTypeAnnotationModelPropertyWrapper;

/**
 * Wraps a {@link ModelProperty} adding type information.
 *
 * @since 1.8
 */
public interface TypeAnnotationModelPropertyWrapper extends TypeAnnotation {

  static TypeAnnotationModelPropertyWrapper defaultTypeAnnotationModelPropertyWrapper(ModelProperty modelProperty) {
    return new DefaultTypeAnnotationModelPropertyWrapper(modelProperty);
  }

  /**
   * @return the wrapped {@link ModelProperty}.
   */
  ModelProperty asModelProperty();

}
