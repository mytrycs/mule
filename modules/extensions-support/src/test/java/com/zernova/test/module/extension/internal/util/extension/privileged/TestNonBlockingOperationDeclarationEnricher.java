/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.internal.util.extension.privileged;

import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.module.extension.api.loader.java.property.CompletableComponentExecutorModelProperty;

public class TestNonBlockingOperationDeclarationEnricher implements DeclarationEnricher {

  @Override
  public void enrich(ExtensionLoadingContext extensionLoadingContext) {
    extensionLoadingContext.getExtensionDeclarer().getDeclaration().getOperations().stream()
        .filter(o -> o.getName().equals("doSomethingAsync"))
        .findFirst()
        .ifPresent(operation -> operation.addModelProperty(
                                                           new CompletableComponentExecutorModelProperty((model,
                                                                                                          params) -> new PrivilegedNonBlockingComponentExecutor())));
  }
}
