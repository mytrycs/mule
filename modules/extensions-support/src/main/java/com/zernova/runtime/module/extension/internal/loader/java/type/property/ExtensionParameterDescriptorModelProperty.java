/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.type.property;

import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionParameter;

/**
 * Binds a {@link ParameterModel} with a {@link ExtensionParameter}
 *
 * @since 4.1
 */
public class ExtensionParameterDescriptorModelProperty implements ModelProperty {

  private static final long serialVersionUID = -4201515069957560449L;

  private final ExtensionParameter extensionParameter;

  public ExtensionParameterDescriptorModelProperty(ExtensionParameter extensionParameter) {
    this.extensionParameter = extensionParameter;
  }

  public ExtensionParameter getExtensionParameter() {
    return extensionParameter;
  }

  @Override
  public String getName() {
    return "extension-parameter";
  }

  @Override
  public boolean isPublic() {
    return false;
  }
}
