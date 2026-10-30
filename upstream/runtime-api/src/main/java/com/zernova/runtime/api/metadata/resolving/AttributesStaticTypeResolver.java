/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.metadata.resolving;

import com.zernova.api.annotation.NoExtend;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.meta.model.OutputModel;
import com.zernova.runtime.api.metadata.MetadataContext;

/**
 * {@link StaticResolver} implementation for the attributes {@link OutputModel}.
 * <p>
 * This {@link AttributesTypeResolver} is discarded once the extension model is built. It's not registered with the dynamic
 * {@link AttributesTypeResolver}s.
 *
 * @since 1.1
 */
@NoExtend
public abstract class AttributesStaticTypeResolver implements AttributesTypeResolver, StaticResolver {

  /**
   * {@inheritDoc}
   * <p>
   * The category name is not used for {@link StaticResolver}s since they are not registered in the extension.
   */
  @Override
  public String getCategoryName() {
    return "ATTRIBUTES_" + STATIC_RESOLVER_NAME;
  }

  /**
   * This method resolves the static output type for an attributes {@link OutputModel} proxing to the
   * {@link StaticResolver#getStaticMetadata()} method.
   * <p>
   * The context and key are not used nor injected.
   */
  @Override
  final public MetadataType getAttributesType(MetadataContext context, Object key) {
    return getStaticMetadata();
  }
}
