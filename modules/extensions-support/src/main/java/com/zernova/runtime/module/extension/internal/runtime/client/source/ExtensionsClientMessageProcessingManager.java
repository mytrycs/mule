/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.client.source;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.core.internal.util.FunctionalUtils.withNullEvent;

import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.core.internal.execution.FlowProcessTemplate;
import com.zernova.runtime.core.internal.execution.MessageProcessContext;
import com.zernova.runtime.core.internal.execution.MessageProcessingManager;
import com.zernova.runtime.core.internal.execution.SourceResultAdapter;
import com.zernova.runtime.extension.api.client.ExtensionsClient;
import com.zernova.runtime.extension.api.client.source.SourceResultHandler;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.module.extension.internal.runtime.source.ExtensionsFlowProcessingTemplate;

import java.util.function.Consumer;

/**
 * Implementation of {@link MessageProcessingManager} for sources created through the {@link ExtensionsClient}
 *
 * @since 4.5.0
 */
class ExtensionsClientMessageProcessingManager<T, A> implements MessageProcessingManager {

  private final SourceClient sourceClient;
  private final Consumer<SourceResultHandler<T, A>> handlerConsumer;

  ExtensionsClientMessageProcessingManager(SourceClient sourceClient,
                                           Consumer<SourceResultHandler<T, A>> handlerConsumer) {
    this.sourceClient = sourceClient;
    this.handlerConsumer = handlerConsumer;
  }

  @Override
  public void processMessage(FlowProcessTemplate template, MessageProcessContext messageProcessContext) {
    checkArgument(template instanceof ExtensionsFlowProcessingTemplate, "invalid processing template");
    ExtensionsFlowProcessingTemplate process = (ExtensionsFlowProcessingTemplate) template;
    SourceResultAdapter resultAdapter = process.getSourceMessage();

    Result result = getResult(resultAdapter);

    handlerConsumer.accept(new DefaultSourceResultHandler<>(sourceClient, result, process));
  }

  private Result<Object, Object> getResult(SourceResultAdapter adapter) {
    com.zernova.sdk.api.runtime.operation.Result<Object, Object> sdkResult =
        adapter.getPayloadMediaTypeResolver().resolve(adapter.getResult());

    return withNullEvent(event -> {
      EventContext context = event.getContext();
      Result.Builder<Object, Object> builder = Result.builder()
          .output(adapter.getCursorProviderFactory().of(context, sdkResult.getOutput(), context.getOriginatingLocation()));

      if (sdkResult.getMediaType().isPresent()) {
        builder.mediaType(sdkResult.getMediaType().get());
      }
      sdkResult.getMediaType().ifPresent(builder::mediaType);
      sdkResult.getAttributes().ifPresent(builder::attributes);
      sdkResult.getAttributesMediaType().ifPresent(builder::attributesMediaType);
      sdkResult.getByteLength().ifPresent(builder::length);

      return builder.build();
    });
  }
}
