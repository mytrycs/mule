/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.component.execution.CompletableCallback;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.sdk.api.runtime.source.SourceCallbackContext;

import java.util.Map;

import org.slf4j.Logger;

/**
 * A {@link SourceCallbackExecutor} that allows chain the execution of two delegating {@link SourceCallbackExecutor}s.
 *
 * @since 4.1
 */
public class ComposedSourceCallbackExecutor implements SourceCallbackExecutor {

  private static final Logger LOGGER = getLogger(ComposedSourceCallbackExecutor.class);

  private final SourceCallbackExecutor first;
  private final SourceCallbackExecutor then;

  ComposedSourceCallbackExecutor(SourceCallbackExecutor first, SourceCallbackExecutor then) {
    this.first = first;
    this.then = then;
  }

  @Override
  public void execute(CoreEvent event,
                      Map<String, Object> parameters,
                      SourceCallbackContext context,
                      CompletableCallback<Void> callback) {
    first.execute(event, parameters, context, new CompletableCallback<Void>() {

      @Override
      public void complete(Void value) {
        then.execute(event, parameters, context, callback);
      }

      @Override
      public void error(Throwable e) {
        then.execute(event, parameters, context, new CompletableCallback<Void>() {

          @Override
          public void complete(Void value) {
            callback.error(e);
          }

          @Override
          public void error(Throwable e2) {
            LOGGER.error("Caught nested callback exception. Will Propagate the original one", e2);
            callback.error(e);
          }
        });
      }
    });
  }
}
