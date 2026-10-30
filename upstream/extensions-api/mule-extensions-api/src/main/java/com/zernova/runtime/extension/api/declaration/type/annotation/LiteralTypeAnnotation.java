/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.declaration.type.annotation;

import com.zernova.metadata.api.annotation.TypeAnnotation;
import com.zernova.runtime.extension.api.runtime.parameter.Literal;

/**
 * {@link TypeAnnotation} indicating that the real type of the of the annotated type is a {@link Literal}
 *
 * @since 1.0
 * @see Literal
 */
public class LiteralTypeAnnotation implements TypeAnnotation {

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return "literal";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isPublic() {
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof LiteralTypeAnnotation;
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }
}
