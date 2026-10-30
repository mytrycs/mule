/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.api.dsl.model.metadata;

import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.metadata.api.cache.MetadataCacheIdGenerator;
import com.zernova.runtime.metadata.api.cache.MetadataCacheIdGeneratorFactory;
import com.zernova.runtime.metadata.api.locator.ComponentLocator;
import com.zernova.runtime.metadata.internal.cache.ComponentAstBasedMetadataCacheIdGenerator;


/**
 * A {@link ComponentAst} based implementation of a {@link MetadataCacheIdGeneratorFactory}
 *
 * @since 4.1.4, 4.2.0
 */
@Deprecated(since = "4.10", forRemoval = true)
public class ModelBasedMetadataCacheIdGeneratorFactory implements MetadataCacheIdGeneratorFactory<ComponentAst> {

  @Override
  public MetadataCacheIdGenerator<ComponentAst> create(DslResolvingContext context,
                                                       ComponentLocator<ComponentAst> locator) {
    return new ComponentAstBasedMetadataCacheIdGenerator(locator);
  }
}
