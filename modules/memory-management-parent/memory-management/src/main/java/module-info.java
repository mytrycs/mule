/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * This module performs memory management for the runtime.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.memory.management {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.profiling.api;

  exports com.zernova.runtime.api.memory.bytebuffer;

  exports com.zernova.runtime.internal.memory.management to
      com.zernova.runtime.deployment.model.impl,
      com.zernova.runtime.spring.config;
}
