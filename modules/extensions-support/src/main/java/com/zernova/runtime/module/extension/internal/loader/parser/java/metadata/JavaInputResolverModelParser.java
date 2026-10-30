/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.metadata;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.util.ClassUtils.getClassName;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.metadata.resolving.InputTypeResolver;
import com.zernova.runtime.core.api.util.ClassUtils;
import com.zernova.runtime.extension.api.loader.parser.metadata.InputResolverModelParser;
import com.zernova.runtime.module.extension.internal.metadata.ZernovaInputTypeResolverAdapter;


public class JavaInputResolverModelParser implements InputResolverModelParser {

  private final String parameterName;
  private final Class<?> inputTypeResolverDeclarationClass;

  public JavaInputResolverModelParser(String parameterName, Class<?> inputTypeResolverDeclarationClass) {
    this.parameterName = parameterName;
    this.inputTypeResolverDeclarationClass = inputTypeResolverDeclarationClass;
  }

  public String getParameterName() {
    return parameterName;
  }

  public InputTypeResolver getInputResolver() {
    return instantiateResolver(inputTypeResolverDeclarationClass);
  }

  private InputTypeResolver instantiateResolver(Class<?> factoryType) {
    try {
      Object resolver = ClassUtils.instantiateClass(factoryType);
      if (resolver instanceof com.zernova.sdk.api.metadata.resolving.InputTypeResolver) {
        return ZernovaInputTypeResolverAdapter.from(resolver);
      } else {
        return (InputTypeResolver) resolver;
      }
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not create instance of type " + getClassName(factoryType)), e);
    }
  }

}
