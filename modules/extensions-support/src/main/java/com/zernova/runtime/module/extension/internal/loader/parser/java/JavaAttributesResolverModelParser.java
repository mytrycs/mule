/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.util.ClassUtils.getClassName;
import static com.zernova.runtime.module.extension.internal.loader.utils.JavaMetadataTypeResolverUtils.isNullResolver;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.metadata.resolving.AttributesTypeResolver;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.core.api.util.ClassUtils;
import com.zernova.runtime.extension.api.loader.parser.AttributesResolverModelParser;
import com.zernova.runtime.module.extension.internal.metadata.ZernovaAttributesTypeResolverAdapter;

/**
 * {@link AttributesResolverModelParser} for Java based syntax
 *
 * @since 4.5.0
 */
public class JavaAttributesResolverModelParser implements AttributesResolverModelParser {

  private final Class<?> attributesTypeResolverDeclarationClass;
  private final LazyValue<AttributesTypeResolver<?>> instance;

  public JavaAttributesResolverModelParser(Class<?> attributesTypeResolverDeclarationClass) {
    this.attributesTypeResolverDeclarationClass = attributesTypeResolverDeclarationClass;
    this.instance = new LazyValue<>(() -> instantiateResolver(attributesTypeResolverDeclarationClass));
  }

  public JavaAttributesResolverModelParser(AttributesTypeResolver<?> attributesTypeResolver) {
    this.attributesTypeResolverDeclarationClass = attributesTypeResolver.getClass();
    this.instance = new LazyValue<>(attributesTypeResolver);
  }

  public boolean hasAttributesResolver() {
    return !isNullResolver(attributesTypeResolverDeclarationClass);
  }

  public AttributesTypeResolver getAttributesResolver() {
    return instance.get();
  }

  private AttributesTypeResolver instantiateResolver(Class<?> factoryType) {
    try {
      Object resolver = ClassUtils.instantiateClass(factoryType);
      if (resolver instanceof com.zernova.sdk.api.metadata.resolving.AttributesTypeResolver) {
        return ZernovaAttributesTypeResolverAdapter.from(resolver);
      } else {
        return (AttributesTypeResolver) resolver;
      }
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not create instance of type " + getClassName(factoryType)), e);
    }
  }
}
