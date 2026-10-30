/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.metadata;

import static java.lang.String.format;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.resolving.TypeKeysResolver;
import com.zernova.sdk.api.metadata.NullMetadataResolver;

import java.util.HashSet;
import java.util.Set;

/**
 * Adapts a {@link com.zernova.sdk.api.metadata.resolving.TypeKeysResolver} into a {@link TypeKeysResolver}
 *
 * @since 4.5.0
 */
public class ZernovaTypeKeysResolverAdapter implements TypeKeysResolver, ZernovaMetadataTypeResolverAdapter {

  private final com.zernova.sdk.api.metadata.resolving.TypeKeysResolver delegate;

  ZernovaTypeKeysResolverAdapter(com.zernova.sdk.api.metadata.resolving.TypeKeysResolver delegate) {
    this.delegate = delegate;
  }

  public static TypeKeysResolver from(Object resolver) {
    checkArgument(resolver != null, "Cannot adapt null resolver");

    if (resolver instanceof TypeKeysResolver) {
      return (TypeKeysResolver) resolver;
    } else if (resolver instanceof NullMetadataResolver) {
      return new com.zernova.runtime.extension.api.metadata.NullMetadataResolver();
    } else if (resolver instanceof com.zernova.sdk.api.metadata.resolving.TypeKeysResolver) {
      return new ZernovaTypeKeysResolverAdapter((com.zernova.sdk.api.metadata.resolving.TypeKeysResolver) resolver);
    } else {
      throw new IllegalArgumentException(format("Resolver of class '%s' is neither a '%s' nor a '%s'",
                                                resolver.getClass().getName(),
                                                TypeKeysResolver.class.getName(),
                                                com.zernova.sdk.api.metadata.resolving.TypeKeysResolver.class.getName()));
    }
  }

  @Override
  public String getCategoryName() {
    return delegate.getCategoryName();
  }

  @Override
  public Set<MetadataKey> getKeys(MetadataContext context) throws MetadataResolvingException, ConnectionException {
    Set<MetadataKey> metadataKeys = new HashSet<>();
    delegate.getKeys(new SdkMetadataContextAdapter(context))
        .forEach(metadataKey -> metadataKeys.add(new ZernovaMetadataKeyAdapter(metadataKey)));

    return metadataKeys;
  }

  @Override
  public String getResolverName() {
    return delegate.getResolverName();
  }

  @Override
  public Class<?> getDelegateResolverClass() {
    return delegate.getClass();
  }
}
