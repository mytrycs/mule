/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metadata.api.cache;

import com.zernova.runtime.metadata.internal.cache.AstConfigurationMetadataCacheIdGenerator;

/**
 * A Factory of {@link ConfigurationMetadataCacheIdGenerator}
 *
 * @since 4.5
 */
public class ConfigurationMetadataCacheIdGeneratorFactory {

  /**
   * @return a new instance of an implementation of {@link ConfigurationMetadataCacheIdGenerator}
   */
  public ConfigurationMetadataCacheIdGenerator create() {
    return new AstConfigurationMetadataCacheIdGenerator();
  }
}
