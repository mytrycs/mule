/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.declaration.type.annotation;

import com.zernova.metadata.api.annotation.TypeAnnotation;
import com.zernova.runtime.extension.api.annotation.param.ConfigOverride;

/**
 * Marker annotation for a field that is a {@link ConfigOverride}.
 *
 * @since 1.0
 */
public class ConfigOverrideTypeAnnotation implements TypeAnnotation {

  /**
   * Creates a new instance
   */
  public ConfigOverrideTypeAnnotation() {}

  @Override
  public String getName() {
    return "configOverride";
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code true}
   */
  @Override
  public boolean isPublic() {
    return false;
  }
}
