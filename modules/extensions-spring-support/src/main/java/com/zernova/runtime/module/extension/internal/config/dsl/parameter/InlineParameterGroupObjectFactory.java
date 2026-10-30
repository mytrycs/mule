/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.parameter;

import static com.zernova.metadata.java.api.utils.JavaTypeUtils.getType;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static com.zernova.runtime.core.api.util.func.Once.of;

import com.zernova.metadata.api.model.ObjectType;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.util.func.Once.RunOnce;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.internal.config.dsl.AbstractExtensionObjectFactory;
import com.zernova.runtime.module.extension.internal.runtime.objectbuilder.DefaultObjectBuilder;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ObjectBuilderValueResolver;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;

import jakarta.inject.Inject;

/**
 * An {@link AbstractExtensionObjectFactory} to resolve extension objects that can be defined as named top level elements and be
 * placed in the mule registry.
 * <p>
 * The objects are parsed as a {@link ValueResolver}. If that resolver is not static, then a value is obtained using a default
 * {@link CoreEvent} and that value is returned. Otherwise, the dynamic {@link ValueResolver} is returned instead.
 *
 * @since 4.0
 */
public class InlineParameterGroupObjectFactory extends AbstractExtensionObjectFactory<Object> {

  @Inject
  private ReflectionCache reflectionCache;

  private DefaultObjectBuilder builder;
  private Class<Object> objectClass;
  private final ObjectType objectType;
  private final ClassLoader classLoader;
  private final RunOnce initialiser;

  public InlineParameterGroupObjectFactory(ObjectType type, ClassLoader classLoader, ZernovaContext muleContext) {
    super(muleContext);
    this.classLoader = classLoader;
    this.objectType = type;
    initialiser = of(() -> {
      objectClass = getType(type);
      builder = new DefaultObjectBuilder(objectClass, reflectionCache);
    });
  }

  @Override
  public Object doGetObject() throws Exception {
    return withContextClassLoader(classLoader, () -> {
      initialiser.runOnce();

      // TODO MULE-10919 - This logic is similar to that of the resolverset object builder and should
      // be generalized
      resolveParameters(objectType, builder);
      resolveParameterGroups(objectType, builder);

      return new ObjectBuilderValueResolver<>(builder, muleContext);
    }, Exception.class, exception -> {
      throw exception;
    });
  }
}
