/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.runtime.extension.api.client.ExtensionsClient;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.internal.runtime.client.EventedExtensionsClientDecorator;

/**
 * An argument resolver that yields instances of {@link ExtensionsClient}.
 *
 * @since 4.0
 */
public class ExtensionsClientArgumentResolver implements ArgumentResolver<ExtensionsClient> {

  private final ExtensionsClient extensionsClient;

  public ExtensionsClientArgumentResolver(ExtensionsClient extensionsClient) {
    this.extensionsClient = extensionsClient;
  }

  @Override
  public ExtensionsClient resolve(ExecutionContext executionContext) {
    return new EventedExtensionsClientDecorator(extensionsClient, ((ExecutionContextAdapter) executionContext).getEvent());
  }
}
