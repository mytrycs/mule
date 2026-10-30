/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.api.tooling.metadata;

import com.zernova.api.annotation.Experimental;
import com.zernova.api.annotation.NoImplement;
import com.zernova.metadata.message.api.MessageMetadataType;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.MetadataKeysContainer;
import com.zernova.runtime.api.metadata.descriptor.ComponentMetadataDescriptor;
import com.zernova.runtime.api.metadata.descriptor.InputMetadataDescriptor;
import com.zernova.runtime.api.metadata.descriptor.OutputMetadataDescriptor;
import com.zernova.runtime.api.metadata.descriptor.RouterInputMetadataDescriptor;
import com.zernova.runtime.api.metadata.descriptor.ScopeInputMetadataDescriptor;
import com.zernova.runtime.api.metadata.descriptor.TypeMetadataDescriptor;
import com.zernova.runtime.api.metadata.resolving.InputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.MetadataResult;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.TypeKeysResolver;
import com.zernova.runtime.extension.api.metadata.MetadataResolverFactory;
import com.zernova.runtime.module.extension.api.metadata.PropagatedParameterTypeResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ParameterValueResolver;

import java.util.function.Supplier;

/**
 * Resolves a Component's Metadata by coordinating the several moving parts that are affected by the Metadata fetching process, so
 * that such pieces can remain decoupled.
 * <p/>
 * This mediator will coordinate the resolvers: {@link MetadataResolverFactory}, {@link TypeKeysResolver},
 * {@link InputTypeResolver} and {@link OutputTypeResolver}, and the descriptors that represent their results:
 * {@link ComponentMetadataDescriptor}, {@link OutputMetadataDescriptor} and {@link TypeMetadataDescriptor}
 *
 * @since 4.8
 */
@NoImplement
public interface MetadataMediator {

  /**
   * Resolves the {@link ScopeInputMetadataDescriptor}. Only to be used for scope components
   * <p>
   * <b>NOTE:</b> Experimental feature. Backwards compatibility not guaranteed.
   *
   * @param context                         current {@link MetadataContext} that will be used by the metadata resolvers.
   * @param key                             {@link MetadataKey} of the type which structure has to be resolved, used both for
   *                                        input and output types
   * @param scopeInputMessageType           a {@link MessageMetadataType} for the message that originally entered the scope
   * @param propagatedParameterTypeResolver Allows for enriching the parameter type resolution with propagation information.
   * @return a {@link MetadataResult} of {@link ScopeInputMetadataDescriptor}
   * @since 4.8.0
   */
  @Experimental
  MetadataResult<ScopeInputMetadataDescriptor> getScopeInputMetadata(MetadataContext context,
                                                                     MetadataKey key,
                                                                     Supplier<MessageMetadataType> scopeInputMessageType,
                                                                     PropagatedParameterTypeResolver propagatedParameterTypeResolver);

  /**
   * Resolves the {@link RouterInputMetadataDescriptor}. Only to be used for router components
   * <p>
   * <b>NOTE:</b> Experimental feature. Backwards compatibility not guaranteed.
   *
   * @param context                         current {@link MetadataContext} that will be used by the metadata resolvers.
   * @param key                             {@link MetadataKey} of the type which structure has to be resolved, used both for
   *                                        input and output types
   * @param routerInputMessageType          a {@link MessageMetadataType} for the message that originally entered the router
   * @param propagatedParameterTypeResolver Allows for enriching the parameter type resolution with propagation information.
   * @return a {@link MetadataResult} of {@link RouterInputMetadataDescriptor}
   * @since 4.8.0
   */
  @Experimental
  MetadataResult<RouterInputMetadataDescriptor> getRouterInputMetadata(MetadataContext context,
                                                                       MetadataKey key,
                                                                       Supplier<MessageMetadataType> routerInputMessageType,
                                                                       PropagatedParameterTypeResolver propagatedParameterTypeResolver);

  MetadataResult<InputMetadataDescriptor> getInputMetadata(MetadataContext context, MetadataKey key);

  MetadataResult<OutputMetadataDescriptor> getOutputMetadata(MetadataContext context, MetadataKey key);

  MetadataResult<MetadataKeysContainer> getMetadataKeys(MetadataContext context,
                                                        ParameterValueResolver metadataKeyResolver);

  MetadataResult<MetadataKeysContainer> getMetadataKeys(MetadataContext context,
                                                        MetadataKey partialKey);

}
