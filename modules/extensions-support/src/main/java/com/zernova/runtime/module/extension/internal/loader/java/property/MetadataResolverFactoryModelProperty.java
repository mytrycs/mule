/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.property;

import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.extension.api.metadata.MetadataResolverFactory;

import java.util.function.Supplier;

/**
 * A model property which allows the enriched component to specify its own {@link MetadataResolverFactory}
 *
 * @since 4.0
 */
public final class MetadataResolverFactoryModelProperty implements ModelProperty {

  private final LazyValue<MetadataResolverFactory> metadataResolverFactory;

  /**
   * Creates a new instance
   *
   * @param metadataResolverFactory a {@link MetadataResolverFactory}
   */
  public MetadataResolverFactoryModelProperty(Supplier<MetadataResolverFactory> metadataResolverFactory) {
    this.metadataResolverFactory = new LazyValue<>(metadataResolverFactory);
  }

  /**
   * @return a {@link MetadataResolverFactory}
   */
  public MetadataResolverFactory getMetadataResolverFactory() {
    return metadataResolverFactory.get();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return "metadataResolverFactory";
  }

  /**
   * @return {@code false}
   */
  @Override
  public boolean isPublic() {
    return false;
  }
}
