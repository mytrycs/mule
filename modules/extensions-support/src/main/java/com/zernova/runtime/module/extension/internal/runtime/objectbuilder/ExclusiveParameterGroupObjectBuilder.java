/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.objectbuilder;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.runtime.extension.api.declaration.type.annotation.ExclusiveOptionalsTypeAnnotation;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ExpressionBasedValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.RequiredParameterValueResolverWrapper;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;

import java.lang.reflect.Field;

/**
 * {@link DefaultObjectBuilder} extension that validates that the built object complies with the rules specified in
 * {@link com.zernova.runtime.extension.api.annotation.param.ExclusiveOptionals}
 *
 * @since 4.0
 */
public final class ExclusiveParameterGroupObjectBuilder<T> extends DefaultObjectBuilder<T> {

  private final ExclusiveOptionalsTypeAnnotation exclusiveOptionalsTypeAnnotation;

  /**
   * Creates a new instance that will build instances of {@code prototypeClass}.
   *
   * @param prototypeClass a {@link Class} which needs to have a public default constructor
   */
  public ExclusiveParameterGroupObjectBuilder(Class<T> prototypeClass,
                                              ExclusiveOptionalsTypeAnnotation exclusiveOptionalsTypeAnnotation,
                                              ReflectionCache reflectionCache) {
    super(prototypeClass, reflectionCache);
    this.exclusiveOptionalsTypeAnnotation = exclusiveOptionalsTypeAnnotation;
  }

  @Override
  public ObjectBuilder<T> addPropertyResolver(String propertyName, ValueResolver<? extends Object> resolver) {
    checkArgument(!isBlank(propertyName), "property name cannot be blank");
    checkArgument(resolver != null, "resolver cannot be null");

    return super.addPropertyResolver(propertyName, wrapResolver(propertyName, resolver));
  }

  @Override
  public ObjectBuilder<T> addPropertyResolver(Field field, ValueResolver<? extends Object> resolver) {
    checkArgument(resolver != null, "resolver cannot be null");

    return super.addPropertyResolver(field, wrapResolver(field.getName(), resolver));
  }

  private ValueResolver<? extends Object> wrapResolver(String propertyName, ValueResolver<? extends Object> resolver) {
    if (exclusiveOptionalsTypeAnnotation.isOneRequired() && resolver.isDynamic()
        && exclusiveOptionalsTypeAnnotation.getExclusiveParameterNames().contains(propertyName)) {
      if (resolver instanceof ExpressionBasedValueResolver) {
        resolver = new RequiredParameterValueResolverWrapper<>(resolver, propertyName,
                                                               ((ExpressionBasedValueResolver) resolver).getExpression());
      } else {
        resolver = new RequiredParameterValueResolverWrapper<>(resolver, propertyName);
      }
    }
    return resolver;
  }

}
