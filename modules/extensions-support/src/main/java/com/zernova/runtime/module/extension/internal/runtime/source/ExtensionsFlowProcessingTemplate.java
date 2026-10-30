/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source;

import static com.zernova.runtime.core.api.rx.Exceptions.propagateWrappingFatal;

import com.zernova.runtime.api.component.execution.CompletableCallback;
import com.zernova.runtime.api.functional.Either;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.util.func.CheckedFunction;
import com.zernova.runtime.core.internal.execution.FlowProcessingTemplate;
import com.zernova.runtime.core.internal.execution.NotificationFunction;
import com.zernova.runtime.core.internal.execution.SourceResultAdapter;
import com.zernova.runtime.core.privileged.exception.MessagingException;

import java.util.List;
import java.util.Map;

public final class ExtensionsFlowProcessingTemplate extends FlowProcessingTemplate {

  private SourceResultAdapter sourceMessage;
  private final SourceCompletionHandler completionHandler;

  ExtensionsFlowProcessingTemplate(SourceResultAdapter sourceMessage,
                                   Processor messageProcessor,
                                   List<NotificationFunction> notificationFunctions, SourceCompletionHandler completionHandler) {
    super(messageProcessor, notificationFunctions);
    this.sourceMessage = sourceMessage;
    this.completionHandler = completionHandler;
  }

  @Override
  public CheckedFunction<CoreEvent, Map<String, Object>> getSuccessfulExecutionResponseParametersFunction() {
    return completionHandler::createResponseParameters;
  }

  @Override
  public CheckedFunction<CoreEvent, Map<String, Object>> getFailedExecutionResponseParametersFunction() {
    return completionHandler::createFailureResponseParameters;
  }

  @Override
  public SourceResultAdapter getSourceMessage() {
    return sourceMessage;
  }

  @Override
  public void sendResponseToClient(CoreEvent response, Map<String, Object> parameters, CompletableCallback<Void> callback) {
    completionHandler.onCompletion(response, parameters, callback);
  }

  @Override
  public void sendFailureResponseToClient(MessagingException exception,
                                          Map<String, Object> parameters,
                                          CompletableCallback<Void> callback) {
    completionHandler.onFailure(exception, parameters, callback);
  }

  @Override
  public void afterPhaseExecution(Either<MessagingException, CoreEvent> either) {
    try {
      completionHandler.onTerminate(either);
    } catch (Exception e) {
      throw propagateWrappingFatal(e);
    } finally {
      sourceMessage = null;
    }
  }

}
