/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.metadata.chain;

import static com.zernova.runtime.api.metadata.resolving.FailureCode.UNKNOWN;
import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.VOID_TYPE;

import com.zernova.metadata.api.builder.UnionTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.UnionType;
import com.zernova.metadata.message.api.MessageMetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.RouterOutputMetadataContext;
import com.zernova.runtime.api.metadata.resolving.AttributesTypeResolver;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * {@link OutputTypeResolver} implementation for Routers.
 * <p>
 * This {@link OutputTypeResolver} propagates the metadata the union of every route's output. This symbolizes that the result will
 * be the output of (any) one of the routes.
 *
 * @since 4.7
 */
public class OneOfRoutesOutputTypeResolver implements OutputTypeResolver<Object>, AttributesTypeResolver<Object> {

  public static final OneOfRoutesOutputTypeResolver INSTANCE = new OneOfRoutesOutputTypeResolver();

  private OneOfRoutesOutputTypeResolver() {}

  @Override
  public String getCategoryName() {
    return "OUTPUT_ROUTER_DYNAMIC";
  }

  @Override
  public String getResolverName() {
    return "ONE_OF_ROUTES";
  }

  @Override
  public MetadataType getOutputType(MetadataContext context, Object key) throws MetadataResolvingException, ConnectionException {
    return union(context, MessageMetadataType::getPayloadType);
  }

  @Override
  public MetadataType getAttributesType(MetadataContext context, Object key)
      throws MetadataResolvingException, ConnectionException {
    return union(context, MessageMetadataType::getAttributesType);
  }

  private MetadataType union(MetadataContext context, Function<MessageMetadataType, Optional<MetadataType>> extractor)
      throws MetadataResolvingException {
    UnionTypeBuilder builder = context.getTypeBuilder().unionType().flattens().removesRepetitions();
    Map<String, Supplier<MessageMetadataType>> routes = context.getRouterOutputMetadataContext()
        .map(RouterOutputMetadataContext::getRouteOutputMessageTypes)
        .orElseThrow(() -> new MetadataResolvingException("Route propagation context not available", UNKNOWN));
    if (routes.isEmpty()) {
      return VOID_TYPE;
    }
    routes.values().forEach(route -> {
      MetadataType type = extractor.apply(route.get()).orElse(VOID_TYPE);
      builder.of(type);
    });
    UnionType result = builder.build();
    if (result.getTypes().size() > 1) {
      return result;
    } else {
      return result.getTypes().get(0);
    }
  }
}
