/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/**
 * Runtime support for Metadata Caches.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.metadata.support {

  requires com.zernova.runtime.metadata.model.api;
  requires com.zernova.runtime.metadata.model.message;
  requires transitive com.zernova.runtime.api;
  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.dsl.api;
  requires com.zernova.runtime.artifact.ast;
  requires com.zernova.runtime.core;
  requires com.zernova.sdk.api;

  requires com.google.common;
  requires org.apache.commons.lang3;

  exports com.zernova.runtime.metadata.api.dsl;
  exports com.zernova.runtime.metadata.api.cache;
  exports com.zernova.runtime.metadata.api.locator;

  exports com.zernova.runtime.metadata.internal to
      com.zernova.runtime.extensions.support,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.tooling.support,
      spring.beans;
  exports com.zernova.runtime.metadata.internal.cache to
      com.zernova.runtime.spring.config,
      com.zernova.runtime.tooling.support,
      spring.beans;

  opens com.zernova.runtime.metadata.internal to
      spring.core;
  opens com.zernova.runtime.metadata.internal.cache to
      spring.core;

}
