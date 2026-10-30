/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.type.property;

import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.module.extension.api.loader.java.type.MethodElement;
import com.zernova.runtime.module.extension.api.loader.java.type.OperationElement;
import com.zernova.runtime.module.extension.api.loader.java.type.Type;

/**
 * Binds an {@link OperationModel} with a {@link MethodElement}
 *
 * @since 4.1
 */
public class ExtensionOperationDescriptorModelProperty implements ModelProperty {

  private final OperationElement operationElement;
  private final boolean hasDeprecatedRouterCompletionCallback;

  public ExtensionOperationDescriptorModelProperty(OperationElement operationMethod) {
    this(operationMethod, false);
  }

  public ExtensionOperationDescriptorModelProperty(OperationElement operationMethod,
                                                   boolean hasDeprecatedRouterCompletionCallback) {
    this.operationElement = operationMethod;
    this.hasDeprecatedRouterCompletionCallback = hasDeprecatedRouterCompletionCallback;
  }

  public OperationElement getOperationElement() {
    return operationElement;
  }

  @Override
  public String getName() {
    return "operation-method-element";
  }

  @Override
  public boolean isPublic() {
    return false;
  }

  public boolean hasDeprecatedRouterCompletionCallback() {
    return hasDeprecatedRouterCompletionCallback;
  }
}
