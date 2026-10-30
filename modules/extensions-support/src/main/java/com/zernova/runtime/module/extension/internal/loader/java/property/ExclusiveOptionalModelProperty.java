/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.property;

import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.parameter.ExclusiveParametersModel;

/**
 * A {@link ModelProperty} intended to be used to signal that the parameter is resolved as part of an
 * {@link ExclusiveParametersModel}
 *
 * @since 4.1
 */
public class ExclusiveOptionalModelProperty implements ModelProperty {

  private ExclusiveParametersModel exclusiveParametersModel;

  /**
   * Creates a new instance
   *
   * @param exclusiveParametersModel a {@link ExclusiveParametersModel}
   */
  public ExclusiveOptionalModelProperty(ExclusiveParametersModel exclusiveParametersModel) {
    this.exclusiveParametersModel = exclusiveParametersModel;
  }

  /**
   * @return the underlying {@link ExclusiveParametersModel#isOneRequired()} value.
   */
  public boolean isOneRequired() {
    return exclusiveParametersModel.isOneRequired();
  }

  /**
   * @return {@code exclusiveOptional}
   */
  @Override
  public String getName() {
    return "exclusiveOptional";
  }

  /**
   * @return {@code false}
   */
  @Override
  public boolean isPublic() {
    return false;
  }

}
