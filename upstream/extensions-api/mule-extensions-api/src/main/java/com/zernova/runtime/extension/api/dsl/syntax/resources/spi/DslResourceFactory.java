/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.dsl.syntax.resources.spi;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.extension.api.resources.GeneratedResource;
import com.zernova.runtime.extension.api.resources.spi.GeneratedResourceFactory;

import java.util.Optional;

/**
 * Creates a {@link GeneratedResource} instance from an {@link ExtensionModel} instance.
 * <p/>
 * Implementations are to be reusable and thread-safe.
 *
 * @since 1.0
 */
@NoImplement
public interface DslResourceFactory extends GeneratedResourceFactory {

  /**
   * Creates a new {@link GeneratedResource} from the given {@code extensionModel}.
   * <p>
   * Because not every implementation of this class necessarily applies to every {@link ExtensionModel}, this method returns an
   * {@link Optional} which will be empty if the kind of generated resource is not valid for the given {@code extensionModel}
   *
   * @param extensionModel the {@link ExtensionModel} that requires the resource
   * @return an {@link Optional} {@link GeneratedResource}
   */
  Optional<GeneratedResource> generateResource(ExtensionModel extensionModel, DslResolvingContext context);
}
