/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Mule Runtime Errors.
 *
 * @provides com.zernova.runtime.ast.api.error.ErrorTypeRepositoryProvider
 *
 * @moduleGraph
 * @since 4.7
 */
module com.zernova.runtime.errors {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.artifact.ast;

  exports com.zernova.runtime.core.api.error;
  exports com.zernova.runtime.config.internal.error to
      com.zernova.runtime.core,
      com.zernova.runtime.extensions.mule.support,
      com.zernova.runtime.spring.config,
      com.zernova.runtime.artifact.ast.serialization.test;

  provides com.zernova.runtime.ast.api.error.ErrorTypeRepositoryProvider with
      com.zernova.runtime.config.internal.error.CoreErrorTypeRepositoryProvider;

}