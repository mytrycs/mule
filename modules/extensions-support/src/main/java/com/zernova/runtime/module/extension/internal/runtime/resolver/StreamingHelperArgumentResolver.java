/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.core.api.streaming.CursorProviderFactory;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.api.streaming.bytes.CursorStreamProviderFactory;
import com.zernova.runtime.core.api.streaming.object.CursorIteratorProviderFactory;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.extension.api.runtime.streaming.StreamingHelper;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.internal.runtime.streaming.DefaultStreamingHelper;

/**
 * An argument resolver which provides instances of {@link StreamingHelper}
 *
 * @since 4.0
 */
public class StreamingHelperArgumentResolver implements ArgumentResolver<StreamingHelper> {

  private boolean initialized = false;

  private CursorStreamProviderFactory cursorStreamProviderFactory;
  private CursorIteratorProviderFactory cursorIteratorProviderFactory;

  /**
   * {@inheritDoc}
   */
  @Override
  public StreamingHelper resolve(ExecutionContext executionContext) {
    initializeCursorProviderFactoriesIfNeeded(executionContext);
    ExecutionContextAdapter<OperationModel> context = (ExecutionContextAdapter<OperationModel>) executionContext;
    return new DefaultStreamingHelper(cursorStreamProviderFactory, cursorIteratorProviderFactory, context.getEvent(),
                                      context.getComponent().getLocation());
  }

  public void initializeCursorProviderFactoriesIfNeeded(ExecutionContext executionContext) {
    if (!initialized) {
      synchronized (this) {
        if (!initialized) {
          doInitializeCursorProviderFactories(executionContext);
          this.initialized = true;
        }
      }
    }
  }

  private void doInitializeCursorProviderFactories(ExecutionContext executionContext) {
    ExecutionContextAdapter<OperationModel> context = (ExecutionContextAdapter<OperationModel>) executionContext;
    StreamingManager streamingManager = context.getStreamingManager();
    CursorProviderFactory cursorProviderFactory = context.getCursorProviderFactory();
    Pair<CursorStreamProviderFactory, CursorIteratorProviderFactory> cursorProviderFactories =
        streamingManager.getPairFor(cursorProviderFactory);
    cursorStreamProviderFactory = cursorProviderFactories.getFirst();
    cursorIteratorProviderFactory = cursorProviderFactories.getSecond();
  }

}
