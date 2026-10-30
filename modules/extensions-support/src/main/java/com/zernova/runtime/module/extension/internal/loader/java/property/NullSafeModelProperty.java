/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.property;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.extension.api.annotation.param.NullSafe;

/**
 * A {@link ModelProperty} intended to be used on {@link ParameterModel parameters} to signal that if the parameter is resolved to
 * {@code null}, then the runtime should create a default instance, such as described in {@link NullSafe}
 *
 * @since 4.0
 */
public class NullSafeModelProperty implements ModelProperty {

  private final MetadataType defaultType;

  public NullSafeModelProperty(MetadataType defaultType) {
    this.defaultType = defaultType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return "nullSafe";
  }

  public MetadataType defaultType() {
    return defaultType;
  }

  /**
   * @return {@code false}
   */
  @Override
  public boolean isPublic() {
    return false;
  }
}
