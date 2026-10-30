/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import static com.google.common.collect.ImmutableMap.of;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.sdk.api.runtime.operation.Result;
import com.zernova.sdk.api.runtime.source.DistributedTraceContextManager;
import com.zernova.sdk.api.runtime.source.Source;
import com.zernova.sdk.api.runtime.source.SourceCallback;
import com.zernova.sdk.api.runtime.source.SourceCallbackContext;

import java.util.LinkedList;
import java.util.List;

@Alias("pet-source-list")
@MediaType(value = TEXT_PLAIN, strict = false)
public class PetStoreListSource extends Source<List<Result<String, Object>>, Object> {

  @com.zernova.sdk.api.annotation.param.ParameterGroup(name = "Breeder")
  private ExclusivePetBreeder breeder;

  @Override
  public void onStart(SourceCallback<List<Result<String, Object>>, Object> sourceCallback) throws ZernovaException {
    SourceCallbackContext context = sourceCallback.createContext();
    context.getDistributedSourceTraceContext().setRemoteTraceContextMap(of("X-Correlation-ID", "0000-0000"));
    context.setCorrelationId(breeder.getBirds());
    List<Result<String, Object>> listResult = new LinkedList<>();
    listResult.add(Result.<String, Object>builder().output("cat").build());
    listResult.add(Result.<String, Object>builder().output("dog").build());
    listResult.add(Result.<String, Object>builder().output("parrot").build());
    customizeCurrentSpan(context.getDistributedSourceTraceContext());
    sourceCallback.handle(Result.<List<Result<String, Object>>, Object>builder().output(listResult).build(),
                          context);
  }

  private void customizeCurrentSpan(DistributedTraceContextManager distributedTraceContextManager) {
    distributedTraceContextManager.setCurrentSpanName("pet-store-list-modified");
    distributedTraceContextManager.addCurrentSpanAttribute("dog", "Jack, the legendary fake border collie");
  }

  @Override
  public void onStop() {}
}
