/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.metadata;

import static com.zernova.metadata.api.utils.MetadataTypeUtils.isEnum;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.util.ClassUtils.getClassName;

import com.zernova.metadata.api.annotation.EnumAnnotation;
import com.zernova.metadata.api.model.BooleanType;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.metadata.resolving.TypeKeysResolver;
import com.zernova.runtime.core.api.util.ClassUtils;
import com.zernova.runtime.extension.api.loader.parser.metadata.MetadataKeyModelParser;
import com.zernova.runtime.module.extension.internal.metadata.BooleanKeyResolver;
import com.zernova.runtime.module.extension.internal.metadata.EnumKeyResolver;
import com.zernova.runtime.module.extension.internal.metadata.ZernovaTypeKeysResolverAdapter;
import com.zernova.sdk.api.metadata.NullMetadataResolver;
import com.zernova.sdk.api.metadata.resolving.PartialTypeKeysResolver;


/**
 * {@link MetadataKeyModelParser} for Java based syntax
 *
 * @since 4.5.0
 */
public class JavaMetadataKeyModelParser implements MetadataKeyModelParser {

  private final String parameterName;
  private final MetadataType metadataType;
  private final String categoryName;
  private final Class<?> keyIdResolverDeclarationClass;

  public JavaMetadataKeyModelParser(String parameterName, String categoryName, MetadataType metadataType,
                                    Class<?> keyIdResolverDeclarationClass) {
    this.parameterName = parameterName;
    this.metadataType = metadataType;
    this.categoryName = categoryName;
    this.keyIdResolverDeclarationClass = keyIdResolverDeclarationClass;
  }

  @Override
  public boolean hasKeyIdResolver() {
    return (!NullMetadataResolver.class.isAssignableFrom(keyIdResolverDeclarationClass) &&
        !com.zernova.runtime.extension.api.metadata.NullMetadataResolver.class.isAssignableFrom(keyIdResolverDeclarationClass)) ||
        ((metadataType != null) && ((metadataType instanceof BooleanType) || isEnum(metadataType)));
  }

  @Override
  public boolean isPartialKeyResolver() {
    return PartialTypeKeysResolver.class.isAssignableFrom(keyIdResolverDeclarationClass) ||
        com.zernova.runtime.api.metadata.resolving.PartialTypeKeysResolver.class.isAssignableFrom(keyIdResolverDeclarationClass);
  }

  @Override
  public TypeKeysResolver getKeyResolver() {
    TypeKeysResolver typeKeysResolver;
    if (metadataType == null || categoryName == null) {
      typeKeysResolver = instantiateResolver();
    } else if (NullMetadataResolver.class.isAssignableFrom(keyIdResolverDeclarationClass) ||
        com.zernova.runtime.extension.api.metadata.NullMetadataResolver.class.isAssignableFrom(keyIdResolverDeclarationClass)) {
      if (metadataType instanceof BooleanType) {
        typeKeysResolver = new BooleanKeyResolver(categoryName);
      } else if (isEnum(metadataType)) {
        typeKeysResolver = new EnumKeyResolver(metadataType.getAnnotation(EnumAnnotation.class).get(), categoryName);
      } else {
        typeKeysResolver = instantiateResolver();
      }
    } else {
      typeKeysResolver = instantiateResolver();
    }

    return typeKeysResolver;
  }

  public Class<?> keyIdResolverDeclarationClass() {
    return keyIdResolverDeclarationClass;
  }

  @Override
  public MetadataType getMetadataType() {
    return metadataType;
  }

  @Override
  public String getParameterName() {
    return parameterName;
  }

  private TypeKeysResolver instantiateResolver() {
    try {
      Object resolver = ClassUtils.instantiateClass(keyIdResolverDeclarationClass);
      if (resolver instanceof com.zernova.sdk.api.metadata.resolving.TypeKeysResolver) {
        return ZernovaTypeKeysResolverAdapter.from(resolver);
      } else {
        return (TypeKeysResolver) resolver;
      }
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not create instance of type "
          + getClassName(keyIdResolverDeclarationClass)), e);
    }
  }

}
