/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.runner.api;

import static java.util.Optional.of;

import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.extension.api.dsl.syntax.resources.spi.DslResourceFactory;
import com.zernova.runtime.extension.api.resources.GeneratedResource;

import java.util.Optional;

/**
 * Test implementation for generating DSL resources in order to test {@link ExtensionPluginMetadataGenerator}.
 */
public class TestDslResourceFactory implements DslResourceFactory {

  @Override
  public Optional<GeneratedResource> generateResource(ExtensionModel extensionModel, DslResolvingContext dslResolvingContext) {
    return generateResource(extensionModel);
  }

  @Override
  public Optional<GeneratedResource> generateResource(ExtensionModel extensionModel) {
    return of(new GeneratedResource(false, extensionModel.getName().toLowerCase() + ".xsd", new byte[] {1}));
  }

}
