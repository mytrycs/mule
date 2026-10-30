/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.metadata.resolving;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.meta.model.OutputModel;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataResolvingException;

/**
 * {@link StaticResolver} implementation for the {@link OutputModel}s.
 * <p>
 * This {@link OutputTypeResolver} is discarded once the extension model is built. It's not registered with the dynamic
 * {@link OutputTypeResolver}s.
 *
 * @since 1.1
 */
public abstract class OutputStaticTypeResolver implements OutputTypeResolver, StaticResolver {

  /**
   * {@inheritDoc}
   * <p>
   * The category name is not used for {@link StaticResolver}s since they are not registered in the extension.
   */
  @Override
  public String getCategoryName() {
    return "OUTPUT_" + STATIC_RESOLVER_NAME;
  }

  /**
   * This method resolves the static output type for a {@link OutputModel} proxing to the
   * {@link StaticResolver#getStaticMetadata()} method.
   * <p>
   * The context and key are not used nor injected.
   */
  @Override
  final public MetadataType getOutputType(MetadataContext context, Object key) {
    return getStaticMetadata();
  }
}
