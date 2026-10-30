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
import com.zernova.runtime.module.extension.internal.runtime.client.adapter.SdkExtensionsClientAdapter;

/**
 * {@link ArgumentResolver} which resolves to a {@link ExtensionsClient} by delegating into a
 * {@link ExtensionsClientArgumentResolver} and adapting the result.
 *
 * @since 4.5.0
 */
public class SdkExtensionsClientArgumentResolver implements ArgumentResolver<com.zernova.sdk.api.client.ExtensionsClient> {

  private final ExtensionsClientArgumentResolver delegate;

  public SdkExtensionsClientArgumentResolver(ExtensionsClient extensionsClient) {
    delegate = new ExtensionsClientArgumentResolver(extensionsClient);
  }

  @Override
  public com.zernova.sdk.api.client.ExtensionsClient resolve(ExecutionContext executionContext) {
    return new SdkExtensionsClientAdapter(delegate.resolve(executionContext));
  }
}
