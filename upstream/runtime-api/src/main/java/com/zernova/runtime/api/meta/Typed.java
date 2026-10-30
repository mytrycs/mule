/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta;

import com.zernova.api.annotation.NoImplement;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.metadata.MetadataService;

/**
 * A generic contract for a component that has a type.
 *
 * @since 1.0
 */
@NoImplement
public interface Typed {

  /**
   * Returns the {@link MetadataType} of the {@link Typed} component.
   *
   * @return a not {@code null} {@link MetadataType}
   */
  MetadataType getType();

  /**
   * Returns {@code true} if the type of the {@link Typed Component} is of dynamic kind, and has to be discovered during design
   * time using the {@link MetadataService} service.
   *
   * @return {@code true} if {@code this} element type is of dynamic kind
   */
  boolean hasDynamicType();

}
