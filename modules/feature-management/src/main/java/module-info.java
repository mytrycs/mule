/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * This module performs feature management for the runtime.
 *
 * @moduleGraph
 * @since 4.5
 */
module com.zernova.runtime.featureManagement {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.profiling.api;

  requires com.github.benmanes.caffeine;
  requires togglz.core;

  exports com.zernova.runtime.feature.api.management;

  exports com.zernova.runtime.feature.internal.config to
      com.zernova.runtime.core,
      com.zernova.runtime.extensions.support;
  exports com.zernova.runtime.feature.internal.config.profiling to
      com.zernova.runtime.core;
  exports com.zernova.runtime.feature.internal.togglz.config to
      com.zernova.runtime.core;
  exports com.zernova.runtime.feature.internal.togglz.user to
      com.zernova.runtime.core;

  provides org.togglz.core.spi.FeatureManagerProvider
      with com.zernova.runtime.feature.internal.togglz.ZernovaTogglzFeatureManagerProvider;

}
