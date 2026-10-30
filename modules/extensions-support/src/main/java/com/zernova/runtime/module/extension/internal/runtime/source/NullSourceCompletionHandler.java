/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source;

import static java.util.Collections.emptyMap;

import com.zernova.runtime.api.component.execution.CompletableCallback;
import com.zernova.runtime.api.functional.Either;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.privileged.exception.MessagingException;

import java.util.Map;

/**
 * {@code SourceCompletionHandler} that does nothing.
 *
 * @since 4.0
 */
public class NullSourceCompletionHandler implements SourceCompletionHandler {

  @Override
  public void onCompletion(CoreEvent event, Map<String, Object> parameters, CompletableCallback<Void> callback) {
    callback.complete(null);
  }

  @Override
  public void onFailure(MessagingException exception, Map<String, Object> parameters, CompletableCallback<Void> callback) {
    callback.error(exception);
  }

  @Override
  public void onTerminate(Either<MessagingException, CoreEvent> eventOrException) {
    // Nothing to do.
  }

  @Override
  public Map<String, Object> createResponseParameters(CoreEvent event) {
    return emptyMap();
  }

  @Override
  public Map<String, Object> createFailureResponseParameters(CoreEvent event) {
    return emptyMap();
  }
}
