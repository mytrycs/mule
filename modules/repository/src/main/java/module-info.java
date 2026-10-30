/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Mule repository module. Provides access to external dependencies required by the runtime.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.repository {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.artifact;
  requires com.zernova.runtime.core;
  requires com.zernova.runtime.maven.client.api;
  requires org.slf4j;

  exports com.zernova.runtime.module.repository.api;

  exports com.zernova.runtime.module.repository.internal to
      com.zernova.runtime.launcher;

}
