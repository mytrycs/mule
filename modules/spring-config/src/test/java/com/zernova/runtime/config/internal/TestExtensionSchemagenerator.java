/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal;

import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.extension.api.dsl.syntax.resources.spi.ExtensionSchemaGenerator;

public final class TestExtensionSchemagenerator implements ExtensionSchemaGenerator {

  @Override
  public String generate(ExtensionModel extensionModel, DslResolvingContext context) {
    return "";
  }

  @Override
  public String generate(ExtensionModel extensionModel, DslResolvingContext context, DslSyntaxResolver dsl) {
    return "";
  }
}
