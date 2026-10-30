/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.metadata.cache.lazy;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.metadata.api.cache.MetadataCacheIdGenerator;
import com.zernova.runtime.metadata.api.cache.MetadataCacheIdGeneratorFactory;
import com.zernova.runtime.metadata.api.locator.ComponentLocator;

import java.util.function.Function;

import jakarta.inject.Inject;

public class DelegateMetadataCacheIdGeneratorFactory implements MetadataCacheIdGeneratorFactory<ComponentAst>, Initialisable {

  private final Function<Registry, MetadataCacheIdGeneratorFactory<ComponentAst>> metadataCacheIdGeneratorFactorySupplier;

  @Inject
  private Registry registry;

  private MetadataCacheIdGeneratorFactory<ComponentAst> metadataCacheIdGeneratorFactoryDelegate;

  public DelegateMetadataCacheIdGeneratorFactory(Function<Registry, MetadataCacheIdGeneratorFactory<ComponentAst>> metadataCacheIdGeneratorFactorySupplier) {
    this.metadataCacheIdGeneratorFactorySupplier = metadataCacheIdGeneratorFactorySupplier;
  }

  @Override
  public void initialise() throws InitialisationException {
    this.metadataCacheIdGeneratorFactoryDelegate = metadataCacheIdGeneratorFactorySupplier.apply(registry);
  }

  @Override
  public MetadataCacheIdGenerator<ComponentAst> create(DslResolvingContext context, ComponentLocator<ComponentAst> locator) {
    return metadataCacheIdGeneratorFactoryDelegate.create(context, locator);
  }

}
