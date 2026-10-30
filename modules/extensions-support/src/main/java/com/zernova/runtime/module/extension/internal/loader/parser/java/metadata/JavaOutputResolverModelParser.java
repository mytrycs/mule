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
import static com.zernova.runtime.module.extension.internal.loader.utils.JavaMetadataTypeResolverUtils.isNullResolver;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.core.api.util.ClassUtils;
import com.zernova.runtime.extension.api.loader.parser.metadata.OutputResolverModelParser;
import com.zernova.runtime.module.extension.internal.metadata.ZernovaOutputTypeResolverAdapter;

/**
 * {@link OutputResolverModelParser} for Java based syntax
 *
 * @since 4.5.0
 */
public class JavaOutputResolverModelParser implements OutputResolverModelParser {

  private final Class<?> outputTypeResolverDeclarationClass;
  private final LazyValue<OutputTypeResolver<?>> instance;

  public JavaOutputResolverModelParser(Class<?> outputTypeResolverDeclarationClass) {
    this.outputTypeResolverDeclarationClass = outputTypeResolverDeclarationClass;
    this.instance = new LazyValue<>(() -> instantiateResolver(outputTypeResolverDeclarationClass));
  }

  public JavaOutputResolverModelParser(OutputTypeResolver<?> outputTypeResolver) {
    this.outputTypeResolverDeclarationClass = outputTypeResolver.getClass();
    this.instance = new LazyValue<>(outputTypeResolver);
  }

  public boolean hasOutputResolver() {
    return !isNullResolver(outputTypeResolverDeclarationClass);
  }

  public OutputTypeResolver getOutputResolver() {
    return instance.get();
  }

  private OutputTypeResolver instantiateResolver(Class<?> factoryType) {
    try {
      Object resolver = ClassUtils.instantiateClass(factoryType);
      if (resolver instanceof com.zernova.sdk.api.metadata.resolving.OutputTypeResolver) {
        return ZernovaOutputTypeResolverAdapter.from(resolver);
      } else {
        return (OutputTypeResolver) resolver;
      }
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not create instance of type " + getClassName(factoryType)), e);
    }
  }

}
