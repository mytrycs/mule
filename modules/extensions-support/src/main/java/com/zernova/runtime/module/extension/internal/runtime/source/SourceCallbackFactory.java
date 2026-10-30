/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source;


import com.zernova.sdk.api.runtime.source.SourceCallback;

/**
 * A factory to create {@link SourceCallback} instances
 */
@FunctionalInterface
public interface SourceCallbackFactory {

  /**
   * Creates a new {@link SourceCallback}
   *
   * @param completionHandlerFactory a {@link SourceCompletionHandlerFactory}
   * @return a new {@link SourceCallback}
   */
  SourceCallback createSourceCallback(SourceCompletionHandlerFactory completionHandlerFactory);
}
