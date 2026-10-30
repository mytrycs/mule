/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.property;

import com.zernova.runtime.api.meta.model.ModelProperty;

/**
 * Marker {@link ModelProperty} to communicate that the Extension model is being generated during compile time instead of being a
 * runtime model loading.
 *
 * @since 4.1
 */
public class CompileTimeModelProperty implements ModelProperty {

  @Override
  public String getName() {
    return "isCompileTime";
  }

  @Override
  public boolean isPublic() {
    return false;
  }
}
