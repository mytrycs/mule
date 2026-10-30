/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.objectbuilder;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.util.ClassUtils.setContextClassLoader;
import static com.zernova.runtime.module.extension.internal.runtime.objectbuilder.ObjectBuilderUtils.createInstance;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;

/**
 * Default implementation of {@link ResolverSetBasedObjectBuilder} which uses reflection and a default constructor to instantiate
 * the generated objects
 *
 * @param <T> the generic type of the produced objects
 * @since 4.0
 */
public class DefaultResolverSetBasedObjectBuilder<T> extends ResolverSetBasedObjectBuilder<T> {

  private final Class<T> prototypeClass;
  private final ClassLoader prototypeClassClassLoader;

  public DefaultResolverSetBasedObjectBuilder(Class<T> prototypeClass,
                                              ResolverSet resolverSet,
                                              ExpressionManager expressionManager,
                                              ZernovaContext context) {
    super(prototypeClass, resolverSet, expressionManager, context);
    this.prototypeClass = prototypeClass;
    prototypeClassClassLoader = prototypeClass.getClassLoader();
  }

  @Override
  protected T instantiateObject() {
    Thread thread = Thread.currentThread();
    ClassLoader currentClassLoader = thread.getContextClassLoader();
    setContextClassLoader(thread, currentClassLoader, prototypeClassClassLoader);
    try {
      return createInstance(prototypeClass);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not create instance of " + prototypeClass), e);
    } finally {
      setContextClassLoader(thread, prototypeClassClassLoader, currentClassLoader);
    }
  }
}
