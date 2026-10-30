/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metadata.internal;

import com.zernova.runtime.extension.api.metadata.NullMetadataResolver;

import java.util.function.Supplier;

/**
 * {@link Supplier} implementation which returns a {@link NullMetadataResolver} instance. This supplier always returns the same
 * instance.
 *
 * @since 4.0
 */
public class NullMetadataResolverSupplier implements Supplier<NullMetadataResolver> {

  public static final NullMetadataResolverSupplier INSTANCE = new NullMetadataResolverSupplier();
  public static final NullMetadataResolver NULL_METADATA_RESOLVER = new NullMetadataResolver();

  private NullMetadataResolverSupplier() {}

  @Override
  public NullMetadataResolver get() {
    return NULL_METADATA_RESOLVER;
  }
}
