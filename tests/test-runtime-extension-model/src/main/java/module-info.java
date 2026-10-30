/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

import com.zernova.runtime.extension.api.provider.RuntimeExtensionModelProvider;

/**
 * @moduleGraph
 * @since 4.5
 */
module com.zernova.test.runtime.extension.model {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.extension.model;

  requires org.mockito;

  provides RuntimeExtensionModelProvider with
      com.zernova.runtime.module.artifact.activation.internal.extension.discovery.test.TestRuntimeExtensionModelProvider;
}
