/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.metadata.model;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.app.declaration.api.ElementDeclaration;
import com.zernova.runtime.config.api.dsl.model.DslElementModelFactory;
import com.zernova.runtime.metadata.api.locator.ComponentLocator;
import com.zernova.runtime.core.internal.value.cache.ValueProviderCacheId;
import com.zernova.runtime.core.internal.value.cache.ValueProviderCacheIdGenerator;
import com.zernova.runtime.metadata.internal.cache.DslElementBasedValueProviderCacheIdGenerator;

import java.util.Optional;

@NoExtend
public class DeclarationBasedValueProviderCacheIdGenerator implements ValueProviderCacheIdGenerator<ElementDeclaration> {

  private final DslElementModelFactory elementModelFactory;
  private final DslElementBasedValueProviderCacheIdGenerator delegate;

  public DeclarationBasedValueProviderCacheIdGenerator(DslResolvingContext context,
                                                       ComponentLocator<ElementDeclaration> locator) {
    this.elementModelFactory = DslElementModelFactory.getDefault(context);
    this.delegate = new DslElementBasedValueProviderCacheIdGenerator(
                                                                     l -> locator.get(l)
                                                                         .map(e -> elementModelFactory.create(e).orElse(null)));
  }

  @Override
  public Optional<ValueProviderCacheId> getIdForResolvedValues(ElementDeclaration containerComponent, String parameterName) {
    checkArgument(containerComponent != null, "Cannot generate a Cache Key for a 'null' component");
    return elementModelFactory.create(containerComponent).flatMap(dsl -> delegate.getIdForResolvedValues(dsl, parameterName));
  }

  @Override
  public Optional<ValueProviderCacheId> getIdForResolvedValues(ElementDeclaration containerComponent, String parameterName,
                                                               String targetPath) {
    checkArgument(containerComponent != null, "Cannot generate a Cache Key for a 'null' component");
    return elementModelFactory.create(containerComponent)
        .flatMap(dsl -> delegate.getIdForResolvedValues(dsl, parameterName, targetPath));
  }

  // Consider refactoring this in the future to make things simpler. MULE-18743
  protected Optional<ValueProviderCacheId> getIdForDependency(ElementDeclaration elementDeclaration) {
    return elementModelFactory.create(elementDeclaration).flatMap(delegate::resolveIdForInjectedElement);
  }

}
