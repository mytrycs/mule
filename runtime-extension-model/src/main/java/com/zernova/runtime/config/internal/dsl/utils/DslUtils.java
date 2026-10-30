/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.dsl.utils;

import static com.zernova.runtime.api.component.ComponentIdentifier.builder;
import static com.zernova.runtime.config.internal.dsl.processor.xml.provider.OperationDslNamespaceInfoProvider.OPERATION_DSL_NAMESPACE;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.BODY_NAME;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.CORE_PREFIX;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.TRY_SCOPE_NAME;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.ast.api.ComponentAst;

/**
 * Utility class used to make some simple checks on the DSL.
 */
public final class DslUtils {

  public static final ComponentIdentifier OPERATION_BODY_IDENTIFIER =
      builder().namespace(OPERATION_DSL_NAMESPACE).name(BODY_NAME).build();
  public static final ComponentIdentifier TRY_SCOPE_IDENTIFIER = builder().namespace(CORE_PREFIX).name(TRY_SCOPE_NAME).build();

  private DslUtils() {
    // Empty constructor to prevent it from being accidentally instantiated.
  }

  /**
   * Checks whether a given {@link ComponentAst} corresponds to a try scope.
   *
   * @param componentAst the component AST.
   * @return {@code true} if the given {@link ComponentAst} is a try scope, or {@code false} otherwise.
   */
  public static boolean isTryScope(ComponentAst componentAst) {
    return TRY_SCOPE_IDENTIFIER.equals(componentAst.getIdentifier());
  }
}
