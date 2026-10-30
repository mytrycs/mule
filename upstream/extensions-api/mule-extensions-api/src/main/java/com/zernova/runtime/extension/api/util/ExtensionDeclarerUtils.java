/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.util;

import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.api.meta.model.declaration.fluent.NestedRouteDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;

/**
 * Utility methods for analyzing and decomposing {@link ExtensionDeclarer} instances.
 *
 * @since 1.7
 */
public final class ExtensionDeclarerUtils {

  private ExtensionDeclarerUtils() {

  }

  /**
   * @param model a {@link OperationDeclaration}
   * @return Whether the given {@code model} declares a Router
   *
   * @since 1.7.0
   */
  public static boolean isRouter(OperationDeclaration model) {
    return model.getNestedComponents().stream().anyMatch(nested -> nested instanceof NestedRouteDeclaration);
  }
}
