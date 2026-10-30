/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.loader.parser;

import com.zernova.metadata.api.TypeLoader;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.model.ExtensionModelHelper;
import com.zernova.runtime.extension.api.loader.parser.ParameterModelParser;

/**
 * {@link ParameterModelParser} implementation for Mule SDK (optional parameters).
 *
 * @since 4.5.0
 */
public class ZernovaSdkOptionalParameterModelParser extends ZernovaSdkParameterModelParser implements ParameterModelParser {

  private Object defaultValue = null;

  public ZernovaSdkOptionalParameterModelParser(ComponentAst parameter, TypeLoader typeLoader,
                                             ExtensionModelHelper extensionModelHelper) {
    super(parameter, typeLoader, extensionModelHelper);

    parseStructure();
  }

  private void parseStructure() {
    defaultValue = getOptionalParameter(parameterAst, "defaultValue").orElse(null);
  }

  @Override
  public boolean isRequired() {
    return false;
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }
}
