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

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.resolving.InputTypeResolver;
import com.zernova.sdk.api.metadata.NullMetadataResolver;

public class ZernovaInputTypeResolverAdapter implements InputTypeResolver, ZernovaMetadataTypeResolverAdapter {

  private final com.zernova.sdk.api.metadata.resolving.InputTypeResolver delegate;

  ZernovaInputTypeResolverAdapter(com.zernova.sdk.api.metadata.resolving.InputTypeResolver delegate) {
    this.delegate = delegate;
  }

  public static InputTypeResolver from(Object resolver) {
    checkArgument(resolver != null, "Cannot adapt null resolver");

    if (resolver instanceof InputTypeResolver) {
      return (InputTypeResolver) resolver;
    } else if (resolver instanceof NullMetadataResolver) {
      return new com.zernova.runtime.extension.api.metadata.NullMetadataResolver();
    } else if (resolver instanceof com.zernova.sdk.api.metadata.resolving.InputTypeResolver) {
      return new ZernovaInputTypeResolverAdapter((com.zernova.sdk.api.metadata.resolving.InputTypeResolver) resolver);
    } else {
      throw new IllegalArgumentException(format("Resolver of class '%s' is neither a '%s' nor a '%s'",
                                                resolver.getClass().getName(),
                                                InputTypeResolver.class.getName(),
                                                com.zernova.sdk.api.metadata.resolving.InputTypeResolver.class.getName()));
    }
  }

  @Override
  public String getResolverName() {
    return delegate.getResolverName();
  }

  @Override
  public MetadataType getInputMetadata(MetadataContext context, Object key)
      throws MetadataResolvingException, ConnectionException {
    return delegate.getInputMetadata(new SdkMetadataContextAdapter(context), key);
  }

  @Override
  public String getCategoryName() {
    return delegate.getCategoryName();
  }

  @Override
  public Class<?> getDelegateResolverClass() {
    return delegate.getClass();
  }
}
