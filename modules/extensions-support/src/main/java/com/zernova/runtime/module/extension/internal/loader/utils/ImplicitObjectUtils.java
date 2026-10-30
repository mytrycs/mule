/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.utils;

import static com.zernova.runtime.module.extension.internal.runtime.resolver.ResolverUtils.getExpressionBasedValueResolver;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.getContainerName;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.internal.loader.ParameterGroupDescriptor;
import com.zernova.runtime.module.extension.internal.loader.java.property.NullSafeModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.property.ParameterGroupModelProperty;
import com.zernova.runtime.module.extension.internal.runtime.resolver.NullSafeValueResolverWrapper;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ParametersResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.StaticValueResolver;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;

import java.util.Optional;

/**
 * Utilities for creating object with implicit values based on a {@link ParameterizedModel}
 *
 * @since 4.0
 */
public final class ImplicitObjectUtils {

  private ImplicitObjectUtils() {}

  /**
   * Creates a {@link ResolverSet} based on the default values for the {@link ParameterModel}s in the {@code parametrizedModel}.
   * <p>
   * If a {@link ParameterModel} returns {@code null} for {@link ParameterModel#getDefaultValue()} then it's ignored
   *
   * @param model       a model holding the {@link ParameterModel}s to consider
   * @param muleContext the Mule node.
   * @return a {@link ResolverSet}
   */
  public static ResolverSet buildImplicitResolverSet(ParameterizedModel model, ReflectionCache reflectionCache,
                                                     ExpressionManager expressionManager, ZernovaContext muleContext) {
    ResolverSet resolverSet = new ResolverSet(muleContext);
    ParametersResolver parametersResolver =
        ParametersResolver.fromDefaultValues(model, muleContext, reflectionCache, expressionManager);

    for (ParameterGroupModel groupModel : model.getParameterGroupModels()) {
      Optional<ParameterGroupDescriptor> descriptor = groupModel.getModelProperty(ParameterGroupModelProperty.class)
          .map(ParameterGroupModelProperty::getDescriptor);

      if (descriptor.isPresent() && groupModel.getParameterModels().stream().noneMatch(ParameterModel::isRequired)) {
        String groupKey = getContainerName(descriptor.get().getContainer());
        resolverSet.add(groupKey,
                        NullSafeValueResolverWrapper.of(new StaticValueResolver<>(null), descriptor.get().getMetadataType(),
                                                        reflectionCache, expressionManager, muleContext, parametersResolver));
      } else {
        groupModel.getParameterModels().forEach(parameterModel -> {
          Object defaultValue = parameterModel.getDefaultValue();
          ValueResolver<?> resolver;
          if (defaultValue instanceof String) {
            resolver = getExpressionBasedValueResolver((String) defaultValue, parameterModel.getType(), muleContext);
          } else {
            resolver = new StaticValueResolver<>(null);
          }

          if (parameterModel.getModelProperty(NullSafeModelProperty.class).isPresent()) {
            MetadataType metadataType = parameterModel.getModelProperty(NullSafeModelProperty.class).get().defaultType();
            resolver = NullSafeValueResolverWrapper.of(resolver, metadataType, reflectionCache, expressionManager,
                                                       muleContext, parametersResolver);
          }

          resolverSet.add(parameterModel.getName(), resolver);
        });
      }
    }

    return resolverSet;
  }
}
