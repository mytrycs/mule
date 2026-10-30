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
import static com.zernova.runtime.module.extension.api.util.ZernovaExtensionUtils.getInitialiserEvent;
import static com.zernova.runtime.module.extension.internal.component.AnnotatedObjectInvocationHandler.addAnnotationsToClass;

import com.zernova.metadata.api.model.ObjectType;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import com.zernova.runtime.dsl.api.component.ObjectTypeProvider;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.config.dsl.AbstractExtensionObjectFactory;
import com.zernova.runtime.module.extension.internal.runtime.objectbuilder.DefaultObjectBuilder;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ObjectBuilderValueResolver;

/**
 * An {@link AbstractExtensionObjectFactory} to resolve extension objects that can be defined as named top level elements and be
 * placed in the mule registry.
 * <p>
 * The objects are parsed as a {@link ValueResolver}. If that resolver is not static, then a value is obtained using a default
 * {@link CoreEvent} and that value is returned. Otherwise, the dynamic {@link ValueResolver} is returned instead.
 *
 * @since 4.0
 */
public class TopLevelParameterObjectFactory extends AbstractExtensionObjectFactory<Object> implements ObjectTypeProvider {

  private LazyValue<DefaultObjectBuilder> builder;
  private LazyValue<Class<?>> objectClass;
  private final ObjectType objectType;
  private final ClassLoader classLoader;
  private String name;
  private Object staticProduct = null;

  public TopLevelParameterObjectFactory(ObjectType type, ClassLoader classLoader, ZernovaContext muleContext) {
    super(muleContext);
    this.classLoader = classLoader;
    this.objectType = type;

    objectClass = new LazyValue<>(() -> withContextClassLoader(classLoader, () -> {
      // We must add the annotations support with a proxy to avoid the SDK user to clutter the POJO definitions in an extension
      // with the annotations stuff.
      return addAnnotationsToClass(getType(type));
    }));
    builder = new LazyValue<>(() -> new DefaultObjectBuilder(objectClass.get(), reflectionCache));
  }

  @Override
  public Object doGetObject() throws Exception {
    return withContextClassLoader(classLoader, () -> {
      // TODO MULE-10919 - This logic is similar to that of the resolverset object builder and should
      // be generalized

      DefaultObjectBuilder builder = this.builder.get();
      resolveParameters(objectType, builder);
      resolveParameterGroups(objectType, builder);

      injectFields();

      ValueResolver<Object> resolver = new ObjectBuilderValueResolver<>(builder, muleContext);
      if (resolver.isDynamic()) {
        return resolver;
      }

      CoreEvent initialiserEvent = getInitialiserEvent(muleContext);
      try (ValueResolvingContext ctx = ValueResolvingContext.builder(initialiserEvent, expressionManager).build()) {
        staticProduct = resolver.resolve(ctx);
        muleContext.getInjector().inject(staticProduct);
        return staticProduct;
      } finally {
        if (initialiserEvent != null) {
          ((BaseEventContext) initialiserEvent.getContext()).success();
        }
      }
    }, Exception.class, exception -> {
      throw exception;
    });
  }

  private void injectFields() {
    DefaultObjectBuilder builder = this.builder.get();
    builder.setEncoding(muleContext.getConfiguration().getDefaultEncoding());
    if (name != null) {
      builder.setName(name);
    }
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public Class<?> getObjectType() {
    return objectClass.get();
  }
}
