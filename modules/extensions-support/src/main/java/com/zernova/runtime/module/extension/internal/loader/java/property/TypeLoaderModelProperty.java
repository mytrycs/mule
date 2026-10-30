/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.property;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.ModelProperty;

/**
 * Provides access to the same {@link ClassTypeLoader} used when loading the extension when using the {@link ExtensionModel} at a
 * later time.
 *
 * @since 4.9
 */
public class TypeLoaderModelProperty implements ModelProperty {

  private static final long serialVersionUID = 1L;

  private final ClassTypeLoader typeLoader;

  public TypeLoaderModelProperty(ClassTypeLoader typeLoader) {
    this.typeLoader = typeLoader;
  }

  public ClassTypeLoader getTypeLoader() {
    return typeLoader;
  }

  @Override
  public String getName() {
    return "typeLoader";
  }

  @Override
  public boolean isPublic() {
    return false;
  }

}
