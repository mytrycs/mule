/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static java.util.Collections.emptyMap;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;

import java.util.Map;

public class NullResolverSet extends ResolverSet {

  private static final ResolverSetResult EMPTY_RESULT = ResolverSetResult.newBuilder().build();
  public static final ResolverSet INSTANCE = new NullResolverSet();

  private NullResolverSet() {
    super(null);
  }

  @Override
  public ResolverSet add(String key, ValueResolver resolver) {
    throw new UnsupportedOperationException("Immutable NullResolverSet");
  }

  @Override
  public ResolverSet addAll(Map<String, ValueResolver<?>> resolvers) {
    throw new UnsupportedOperationException("Immutable NullResolverSet");
  }

  @Override
  public boolean isDynamic() {
    return false;
  }

  @Override
  public ResolverSetResult resolve(ValueResolvingContext context) throws ZernovaException {
    return EMPTY_RESULT;
  }

  @Override
  public ResolverSet merge(ResolverSet resolverSet) {
    return resolverSet;
  }

  @Override
  public Map<String, ValueResolver<?>> getResolvers() {
    return emptyMap();
  }

  @Override
  public void initialise() throws InitialisationException {}
}
