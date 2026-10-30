/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.objectbuilder;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.api.util.Preconditions.checkState;
import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.getMuleVersion;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.module.extension.api.util.ZernovaExtensionUtils.getInitialiserEvent;
import static com.zernova.runtime.module.extension.internal.runtime.objectbuilder.ObjectBuilderUtils.createInstance;
import static com.zernova.runtime.module.extension.internal.runtime.resolver.ResolverUtils.resolveCursorAsUnclosable;
import static com.zernova.runtime.module.extension.internal.runtime.resolver.ResolverUtils.resolveValue;
import static com.zernova.runtime.module.extension.internal.runtime.resolver.ResolverUtils.typedValueAsUnclosable;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.checkInstantiable;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.getField;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.injectFields;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.hasAnyDynamic;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.module.extension.api.runtime.resolver.ParameterValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingException;
import com.zernova.runtime.module.extension.internal.util.FieldSetter;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;

/**
 * Default implementation of {@link ObjectBuilder} which creates instances through a provided {@link Class}.
 *
 * @since 3.7.0
 */
public class DefaultObjectBuilder<T> implements ObjectBuilder<T>, Initialisable, ParameterValueResolver {

  @Inject
  private ZernovaContext muleContext;

  protected final Class<T> prototypeClass;
  protected final Map<FieldSetter, ValueResolver<Object>> resolvers = new HashMap<>();
  protected final Map<String, ValueResolver<? extends Object>> resolverByFieldName = new HashMap<>();
  protected ReflectionCache reflectionCache;
  private String name = null;
  private String encoding = null;

  /**
   * Creates a new instance that will build instances of {@code prototypeClass}.
   *
   * @param prototypeClass a {@link Class} which needs to have a public defualt constructor
   */
  public DefaultObjectBuilder(Class<T> prototypeClass, ReflectionCache reflectionCache) {
    checkState(reflectionCache != null, "null reflection cache");
    checkInstantiable(prototypeClass, reflectionCache);
    this.prototypeClass = prototypeClass;
    this.reflectionCache = reflectionCache;
  }

  /**
   * Adds a property which value is to be obtained from a {@link ValueResolver}
   *
   * @param propertyName the name of the property in which the value is to be assigned
   * @param resolver     a {@link ValueResolver} used to provide the actual value
   * @return this builder
   * @throws {@link java.lang.IllegalArgumentException} if method or resolver are {@code null}
   */
  public ObjectBuilder<T> addPropertyResolver(String propertyName, ValueResolver<? extends Object> resolver) {
    checkArgument(!isBlank(propertyName), "property name cannot be blank");

    Field field = getField(prototypeClass, propertyName, reflectionCache)
        .orElseThrow(() -> new IllegalArgumentException(format("Class '%s' does not contain property '%s'",
                                                               prototypeClass.getName(), propertyName)));

    return addPropertyResolver(field, resolver);
  }

  /**
   * Adds a property which value is to be obtained from a {@link ValueResolver}
   *
   * @param field    the property in which the value is to be assigned
   * @param resolver a {@link ValueResolver} used to provide the actual value
   * @return this builder
   * @throws {@link java.lang.IllegalArgumentException} if method or resolver are {@code null}
   */
  public ObjectBuilder<T> addPropertyResolver(Field field, ValueResolver<? extends Object> resolver) {
    checkArgument(resolver != null, "resolver cannot be null");

    resolverByFieldName.put(field.getName(), resolver);

    resolvers.put(new FieldSetter<>(field), (ValueResolver<Object>) resolver);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDynamic() {
    return hasAnyDynamic(resolvers.values());
  }

  @Override
  public T build(ValueResolvingContext context) throws ZernovaException {
    T object = createInstance(prototypeClass);

    for (Map.Entry<FieldSetter, ValueResolver<Object>> entry : resolvers.entrySet()) {
      final Object resolvedValue = resolveValue(entry.getValue(), context);

      entry.getKey().set(object,
                         context == null || context.resolveCursors() ? resolveCursorAsUnclosable(resolvedValue)
                             : typedValueAsUnclosable(resolvedValue));
    }

    injectFields(object, name, encoding, getMuleVersion(), reflectionCache);

    return object;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }


  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(resolvers.values(), muleContext);
  }

  @Override
  public Object getParameterValue(String parameterName) throws ValueResolvingException {
    ValueResolver<?> valueResolver = resolverByFieldName.get(parameterName);
    if (valueResolver == null) {
      return null;
    }
    try {
      return valueResolver.resolve(ValueResolvingContext.builder(getInitialiserEvent()).build());
    } catch (Exception e) {
      throw new ValueResolvingException(format("An error occurred trying to resolve value for parameter [%s]", parameterName), e);
    }
  }

  @Override
  public Map<String, ValueResolver<? extends Object>> getParameters() {
    return unmodifiableMap(resolverByFieldName);
  }
}
