/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.annotations;

import com.zernova.metadata.api.annotation.MarkerAnnotation;
import com.zernova.metadata.api.annotation.TypeAnnotation;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.ObjectType;

/**
 * A {@link MarkerAnnotation} to signal that a {@link MetadataType} is a custom built static type.
 *
 * @since 1.1
 */
public class CustomDefinedStaticTypeAnnotation extends MarkerAnnotation {

  public static final String NAME = "customStaticType";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isPublic() {
    return false;
  }
}
