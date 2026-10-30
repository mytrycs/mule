/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.construct;

import static com.zernova.metadata.java.api.utils.JavaTypeUtils.getType;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static com.zernova.runtime.core.api.util.func.Once.of;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.newChain;

import static java.util.Optional.empty;

import com.zernova.metadata.api.model.ObjectType;
import com.zernova.runtime.api.meta.model.nested.NestedChainModel;
import com.zernova.runtime.api.meta.model.nested.NestedRouteModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.api.util.func.Once.RunOnce;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.internal.config.dsl.AbstractExtensionObjectFactory;
import com.zernova.runtime.module.extension.internal.runtime.objectbuilder.DefaultObjectBuilder;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ProcessorChainValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.RouteBuilderValueResolver;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;

import java.util.List;

import jakarta.inject.Inject;

/**
 * An {@link AbstractExtensionObjectFactory} to resolve {@link NestedRouteModel} elements.
 * <p>
 * The objects are parsed as a {@link ValueResolver}. If that resolver is not static, then a value is obtained using a default
 * {@link CoreEvent} and that value is returned. Otherwise, the dynamic {@link ValueResolver} is returned instead.
 *
 * @since 4.0
 */
public class RouteComponentObjectFactory extends AbstractExtensionObjectFactory<Object> {

  private final NestedRouteModel model;
  private final ObjectType objectType;
  private final ClassLoader classLoader;
  private final List<Processor> nestedProcessors;
  private final RunOnce initialiser;
  private DefaultObjectBuilder builder;
  private Class<Object> objectClass;
  @Inject
  private ReflectionCache reflectionCache;

  public RouteComponentObjectFactory(NestedRouteModel model, ObjectType objectType, ClassLoader classLoader,
                                     ZernovaContext muleContext, List<Processor> nestedProcessors) {
    super(muleContext);
    this.classLoader = classLoader;
    this.model = model;
    this.objectType = objectType;
    this.nestedProcessors = nestedProcessors;
    initialiser = of(() -> {
      objectClass = getType(objectType);
      builder = new DefaultObjectBuilder(objectClass, reflectionCache);
    });
  }

  @Override
  public Object doGetObject() throws Exception {

    return withContextClassLoader(classLoader, () -> {
      initialiser.runOnce();

      final MessageProcessorChain chain;

      if (nestedProcessors != null) {
        chain = newChain(empty(), nestedProcessors);
        final StreamingManager streamingManager =
            ((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObject(StreamingManager.class);
        model.getNestedComponents().stream()
            .filter(component -> component instanceof NestedChainModel)
            .findFirst()
            .ifPresent(chainModel -> parameters.put(chainModel.getName(),
                                                    new ProcessorChainValueResolver(model, chain, streamingManager)));
      } else {
        chain = null;
      }

      resolveParameters(objectType, builder);
      resolveParameterGroups(objectType, builder);

      return new RouteBuilderValueResolver(builder, muleContext, chain);

    }, Exception.class, exception -> {
      throw exception;
    });
  }
}
