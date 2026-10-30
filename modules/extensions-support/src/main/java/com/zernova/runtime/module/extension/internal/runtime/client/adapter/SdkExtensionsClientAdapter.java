/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.client.adapter;

import com.zernova.runtime.core.internal.util.message.SdkResultAdapter;
import com.zernova.sdk.api.client.ExtensionsClient;
import com.zernova.sdk.api.client.OperationParameterizer;
import com.zernova.sdk.api.runtime.operation.Result;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Adapts a legacy {@link com.zernova.runtime.extension.api.client.ExtensionsClient} into a {@link ExtensionsClient}
 *
 * @since 4.5.0
 */
public class SdkExtensionsClientAdapter implements ExtensionsClient {

  private final com.zernova.runtime.extension.api.client.ExtensionsClient delegate;

  public SdkExtensionsClientAdapter(com.zernova.runtime.extension.api.client.ExtensionsClient delegate) {
    this.delegate = delegate;
  }

  @Override
  public <T, A> CompletableFuture<Result<T, A>> execute(String extension,
                                                        String operation,
                                                        Consumer<OperationParameterizer> parameters) {
    CompletableFuture<Result<T, A>> future = new CompletableFuture<>();
    delegate.execute(extension, operation, realParams -> parameters.accept(new SdkOperationParameterizerAdapter(realParams)))
        .whenComplete((v, e) -> {
          if (e != null) {
            future.completeExceptionally(e);
          } else {
            future.complete(SdkResultAdapter.from(v));
          }
        });

    return future;
  }
}
