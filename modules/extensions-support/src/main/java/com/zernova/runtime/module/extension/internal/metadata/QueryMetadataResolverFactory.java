/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.metadata;

import static java.util.Collections.emptyList;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.core.api.util.ClassUtils.getClassName;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.metadata.resolving.AttributesTypeResolver;
import com.zernova.runtime.api.metadata.resolving.InputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.QueryEntityResolver;
import com.zernova.runtime.api.metadata.resolving.TypeKeysResolver;
import com.zernova.runtime.core.api.util.ClassUtils;
import com.zernova.runtime.extension.api.annotation.param.Query;
import com.zernova.runtime.extension.api.metadata.MetadataResolverFactory;
import com.zernova.runtime.extension.api.metadata.NullMetadataResolver;

import java.util.Collection;


/**
 * A {@link MetadataResolverFactory} implementation for {@link Query} operations, it provides initialized instances of
 * {@link OutputTypeResolver} and {@link QueryEntityResolver}.
 *
 * {@link AttributesTypeResolver}, {@link InputTypeResolver} and {@link TypeKeysResolver} returned instances are always instances
 * of {@link NullMetadataResolver}.
 *
 * @since 4.0
 */
public final class QueryMetadataResolverFactory implements MetadataResolverFactory {

  private final OutputTypeResolver outputTypeResolver;
  private final QueryEntityResolver queryMetadataEntityResolver;
  private final NullMetadataResolver nullMetadataResolver = new NullMetadataResolver();

  public QueryMetadataResolverFactory(Class<? extends OutputTypeResolver> outputResolver,
                                      Class<? extends QueryEntityResolver> queryEntityResolver) {
    checkArgument(outputResolver != null, "OutputTypeResolver type cannot be null");
    checkArgument(queryEntityResolver != null, "QueryEntityResolver type cannot be null");
    outputTypeResolver = instantiateResolver(outputResolver);
    queryMetadataEntityResolver = instantiateResolver(queryEntityResolver);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TypeKeysResolver getKeyResolver() {
    return nullMetadataResolver;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> InputTypeResolver<T> getInputResolver(String parameterName) {
    return (InputTypeResolver<T>) nullMetadataResolver;
  }

  @Override
  public Collection<InputTypeResolver> getInputResolvers() {
    return emptyList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> AttributesTypeResolver<T> getOutputAttributesResolver() {
    return (AttributesTypeResolver<T>) nullMetadataResolver;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> OutputTypeResolver<T> getOutputResolver() {
    return new DsqlQueryMetadataResolver(queryMetadataEntityResolver, outputTypeResolver);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public QueryEntityResolver getQueryEntityResolver() {
    return queryMetadataEntityResolver;
  }

  private <T> T instantiateResolver(Class<?> factoryType) {
    try {
      return (T) ClassUtils.instantiateClass(factoryType);
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not create NamedTypeResolver of type "
          + getClassName(factoryType)), e);
    }
  }
}
