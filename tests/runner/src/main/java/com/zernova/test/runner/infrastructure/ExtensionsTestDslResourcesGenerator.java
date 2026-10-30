/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.runner.infrastructure;

import static java.util.Collections.emptyList;
import static com.zernova.runtime.api.util.collection.Collectors.toImmutableList;
import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.extension.api.dsl.syntax.resources.spi.DslResourceFactory;
import com.zernova.runtime.extension.api.resources.GeneratedResource;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * This class extends {@link ExtensionsTestLoaderResourcesGenerator} that writes the DSL generated resources to the specified
 * target directory but also exposes the content to be shared for testing purposes.
 *
 * @since 4.0
 */
class ExtensionsTestDslResourcesGenerator extends ExtensionsTestLoaderResourcesGenerator {

  private final List<DslResourceFactory> resourceFactories;
  private final DslResolvingContext context;

  ExtensionsTestDslResourcesGenerator(List<DslResourceFactory> resourceFactories, File generatedResourcesDirectory,
                                      DslResolvingContext context) {
    super(emptyList(), generatedResourcesDirectory);
    this.resourceFactories = ImmutableList.copyOf(resourceFactories);
    this.context = context;
  }

  /**
   * {@inheritDoc}
   */
  public List<GeneratedResource> generateFor(ExtensionModel extensionModel) {
    List<GeneratedResource> resources =
        resourceFactories.stream().map(factory -> factory.generateResource(extensionModel, context))
            .filter(Optional::isPresent).map(Optional::get).collect(toImmutableList());

    resources.forEach(this::write);
    return resources;
  }
}
