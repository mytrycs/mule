/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.api.dsl.model.metadata;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.config.api.dsl.model.DslElementModelFactory;
import com.zernova.runtime.core.internal.value.cache.ValueProviderCacheId;
import com.zernova.runtime.core.internal.value.cache.ValueProviderCacheIdGenerator;
import com.zernova.runtime.metadata.api.locator.ComponentLocator;
import com.zernova.runtime.metadata.internal.cache.DslElementBasedValueProviderCacheIdGenerator;

import java.util.Optional;

@Deprecated(since = "4.10", forRemoval = true)
public class ComponentBasedValueProviderCacheIdGenerator implements ValueProviderCacheIdGenerator<ComponentAst> {

  private final DslElementModelFactory elementModelFactory;
  private final DslElementBasedValueProviderCacheIdGenerator delegate;

  public ComponentBasedValueProviderCacheIdGenerator(DslResolvingContext context,
                                                     ComponentLocator<ComponentAst> locator) {
    this.elementModelFactory = DslElementModelFactory.getDefault(context);
    this.delegate = new DslElementBasedValueProviderCacheIdGenerator(location -> locator.get(location)
        .map(c -> elementModelFactory.create(c)
            .orElse(null)));
  }

  @Override
  public Optional<ValueProviderCacheId> getIdForResolvedValues(ComponentAst containerComponent, String parameterName) {
    checkArgument(containerComponent != null, "Cannot generate a Cache Key for a 'null' component");
    return elementModelFactory.create(containerComponent).flatMap(e -> delegate.getIdForResolvedValues(e, parameterName));
  }

  @Override
  public Optional<ValueProviderCacheId> getIdForResolvedValues(ComponentAst containerComponent, String parameterName,
                                                               String targetPath) {
    checkArgument(containerComponent != null, "Cannot generate a Cache Key for a 'null' component");
    return elementModelFactory.create(containerComponent)
        .flatMap(e -> delegate.getIdForResolvedValues(e, parameterName, targetPath));
  }
}
