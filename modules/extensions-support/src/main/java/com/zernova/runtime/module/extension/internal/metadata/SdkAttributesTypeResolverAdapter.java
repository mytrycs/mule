/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.metadata;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.extension.api.metadata.NullMetadataResolver;
import com.zernova.sdk.api.metadata.MetadataContext;
import com.zernova.sdk.api.metadata.resolving.AttributesTypeResolver;

public class SdkAttributesTypeResolverAdapter implements AttributesTypeResolver {

  private com.zernova.runtime.api.metadata.resolving.AttributesTypeResolver delegate;

  SdkAttributesTypeResolverAdapter(com.zernova.runtime.api.metadata.resolving.AttributesTypeResolver delegate) {
    this.delegate = delegate;
  }

  public static AttributesTypeResolver from(com.zernova.runtime.api.metadata.resolving.AttributesTypeResolver delegate) {
    if (delegate instanceof NullMetadataResolver) {
      return new com.zernova.sdk.api.metadata.NullMetadataResolver();
    } else {
      return new SdkAttributesTypeResolverAdapter(delegate);
    }
  }


  @Override
  public String getCategoryName() {
    return delegate.getCategoryName();
  }

  @Override
  public String getResolverName() {
    return delegate.getResolverName();
  }

  @Override
  public MetadataType getAttributesType(MetadataContext context, Object key)
      throws MetadataResolvingException, ConnectionException {
    return delegate.getAttributesType(new ZernovaMetadataContextAdapter(context), key);
  }
}
