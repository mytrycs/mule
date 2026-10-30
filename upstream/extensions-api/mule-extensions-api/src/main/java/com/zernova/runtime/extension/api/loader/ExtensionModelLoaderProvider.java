/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.loader;

import com.zernova.api.annotation.NoImplement;

import java.util.Set;

/**
 * Contract intended for the discovery of the available {@link ExtensionModelLoader}.
 * <p>
 * This contract was originally design with SPI discovery in mind, but could also be used in different contexts.
 *
 * @since 1.5.0
 */
@NoImplement
public interface ExtensionModelLoaderProvider {

  /**
   * @return a {@link Set} of available {@link ExtensionModelLoader} instances.
   */
  Set<ExtensionModelLoader> getExtensionModelLoaders();
}
