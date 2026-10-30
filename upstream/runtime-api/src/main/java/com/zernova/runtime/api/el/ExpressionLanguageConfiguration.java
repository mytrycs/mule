/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.el;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.meta.ZernovaVersion;

import java.nio.charset.Charset;
import java.util.Optional;

/**
 * Determines all configuration for a {@link ExpressionLanguage} allowing customization.
 *
 * @since 1.2
 */
@NoImplement
public interface ExpressionLanguageConfiguration {

  static ExpressionLanguageConfigurationBuilder builder() {
    return new ExpressionLanguageConfigurationBuilder();
  }

  /**
   * @return the encoding that should be use by default by the expression language
   */
  Charset getDefaultEncoding();

  /**
   * Returns an instance of FeatureFlaggingService with per deployment configured {@link com.zernova.runtime.api.config.Feature}s
   *
   * @return
   * @since 1.4.0, 1.3.1
   */
  FeatureFlaggingService getFeatureFlaggingService();

  /**
   * @return The application ID.
   *
   * @since 1.5.0
   */
  String getAppId();


  /**
   * @return The {@link ZernovaVersion} that the application has configured.
   *
   * @since 1.5.0
   */
  Optional<ZernovaVersion> getMinMuleVersion();

}
