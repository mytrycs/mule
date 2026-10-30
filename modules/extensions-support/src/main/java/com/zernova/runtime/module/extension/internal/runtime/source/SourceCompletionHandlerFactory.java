/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source;

import com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext;

/**
 * A factory to create a {@link SourceCompletionHandler} associated to a {@link SourceCallbackContext}
 *
 * @since 4.0
 */
@FunctionalInterface
public interface SourceCompletionHandlerFactory {

  /**
   * Creates a new {@link SourceCompletionHandler}
   *
   * @param context a {@link SourceCallbackContext}
   * @return a new {@link SourceCompletionHandler}
   */
  SourceCompletionHandler createCompletionHandler(SourceCallbackContextAdapter context);
}
